package vortex.gateway

import com.my.vortex.domain.interfaces.Logger
import com.my.vortex.domain.interfaces.NoOpLogger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import vortex.gateway.entities.Heartbeat
import vortex.gateway.entities.Identify.Companion.toIdentifyPayload
import vortex.gateway.entities.OutgoingPayload
import vortex.gateway.entities.Payload
import vortex.gateway.entities.PayloadData
import vortex.gateway.entities.Ready
import vortex.gateway.entities.Resume
import vortex.gateway.entities.op.OpCode
import vortex.gateway.entities.op.OpCode.*
import vortex.gateway.entities.presence.Presence
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

open class DiscordWebSocketImpl(
    private val token: String,
    private val logger: Logger = NoOpLogger
) : DiscordWebSocket {
    private val gatewayUrl = "wss://gateway.discord.gg/?v=10&encoding=json"
    private val scopeJob = SupervisorJob()
    override val coroutineContext: CoroutineContext = scopeJob + Dispatchers.IO
    private val client = HttpClient { install(WebSockets) }
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val connectionLock = Any()
    private val presenceMutex = Mutex()
    @Volatile private var connectionJob: Job? = null
    private var heartbeatJob: Job? = null
    private var handshakeJob: Job? = null
    @Volatile private var websocket: DefaultClientWebSocketSession? = null
    @Volatile private var connected = false
    @Volatile private var closed = false
    @Volatile private var heartbeatAcknowledged = true
    @Volatile private var sequence = 0
    @Volatile private var latestPresence: Presence? = null
    private var sessionId: String? = null
    private var resumeGatewayUrl: String? = null
    private var readyAtMillis = 0L

    override suspend fun connect() {
        synchronized(connectionLock) {
            if (closed || connectionJob?.isActive == true) return
            connectionJob = launch { connectionLoop() }
        }
    }

    private suspend fun connectionLoop() {
        var attempt = 0
        while (isActive && !closed) {
            var closeCode: Int? = null
            var session: DefaultClientWebSocketSession? = null
            readyAtMillis = 0L
            try {
                logger.i("Gateway", "Connecting")
                val activeSession = client.webSocketSession(resumeGatewayUrl ?: gatewayUrl)
                session = activeSession
                websocket = activeSession
                handshakeJob = launch {
                    delay(30_000)
                    if (!connected && websocket === activeSession) {
                        logger.w("Gateway", "Handshake timed out; reconnecting")
                        activeSession.cancel()
                    }
                }
                for (frame in activeSession.incoming) {
                    if (frame is Frame.Text) onMessage(frame.readText())
                }
                closeCode = readCloseCode(activeSession)
            } catch (e: CancellationException) {
                if (!isActive || closed) throw e
                logger.w("Gateway", "Connection interrupted; reconnecting")
                closeCode = session?.let { readCloseCode(it) }
            } catch (e: Exception) {
                logger.w("Gateway", "Connection interrupted: ${e.javaClass.simpleName}")
                closeCode = session?.let { readCloseCode(it) }
            } finally {
                heartbeatJob?.cancel()
                heartbeatJob = null
                handshakeJob?.cancel()
                handshakeJob = null
                connected = false
                websocket = null
                session?.cancel()
            }
            if (!isActive || closed) return
            if (!GatewayRetryPolicy.canRetry(closeCode)) {
                logger.e("Gateway", "Connection rejected (code $closeCode); sign in again or check account settings")
                return
            }
            if (GatewayRetryPolicy.shouldClearSession(closeCode)) clearSession()
            if (readyAtMillis != 0L && System.currentTimeMillis() - readyAtMillis >= 30_000L) attempt = 0
            val wait = GatewayRetryPolicy.delayMillis(attempt++) + Random.nextLong(0, 500)
            logger.w("Gateway", "Disconnected (code $closeCode); retrying in ${wait / 1000}s")
            delay(wait)
        }
    }

    private suspend fun readCloseCode(session: DefaultClientWebSocketSession): Int? = try {
        withTimeoutOrNull(1_000) { session.closeReason.await()?.code?.toInt() }
    } catch (e: CancellationException) {
        if (!currentCoroutineContext().isActive) throw e
        null
    } catch (e: Exception) {
        null
    }

    private suspend fun onMessage(jsonString: String) {
        val payload = json.decodeFromString<Payload>(jsonString)
        payload.s?.let { sequence = it }
        when (payload.op) {
            DISPATCH -> {
                payload.handleDispatch(jsonString)
                if (payload.t == "READY" || payload.t == "RESUMED") publishLatestPresence()
            }
            HEARTBEAT -> sendHeartbeat()
            HEARTBEAT_ACK -> heartbeatAcknowledged = true
            RECONNECT -> reconnectWebSocket()
            INVALID_SESSION -> handleInvalidSession(jsonString)
            HELLO -> handleHello(jsonString)
            else -> Unit
        }
    }

    open fun Payload.handleDispatch(jsonString: String) {
        when (t) {
            "READY" -> {
                val ready = decodePayloadData<Ready>(jsonString) ?: return
                sessionId = ready.sessionId
                resumeGatewayUrl = ready.resumeGatewayUrl?.trimEnd('/')?.let { "$it/?v=10&encoding=json" }
                connected = true
                handshakeJob?.cancel()
                readyAtMillis = System.currentTimeMillis()
                logger.i("Gateway", "Session ready")
            }
            "RESUMED" -> {
                connected = true
                handshakeJob?.cancel()
                readyAtMillis = System.currentTimeMillis()
                logger.i("Gateway", "Session resumed")
            }
        }
    }

    private suspend fun handleInvalidSession(jsonString: String) {
        val resumable = decodePayloadData<Boolean>(jsonString) == true
        if (!resumable) clearSession()
        delay(Random.nextLong(1_000, 5_001))
        if (resumable && sessionId != null && sequence > 0) sendResume() else sendIdentify()
    }

    private suspend fun handleHello(jsonString: String) {
        val interval = decodePayloadData<Heartbeat>(jsonString)?.heartbeatInterval ?: return
        if (interval <= 0) return
        heartbeatAcknowledged = true
        startHeartbeatJob(interval)
        if (sequence > 0 && !sessionId.isNullOrBlank()) sendResume() else sendIdentify()
    }

    protected fun decodeReady(jsonString: String): Ready? = decodePayloadData(jsonString)

    private inline fun <reified T> decodePayloadData(jsonString: String): T? =
        json.decodeFromString<PayloadData<T>>(jsonString).d

    private suspend fun sendHeartbeat() {
        heartbeatAcknowledged = false
        send(HEARTBEAT, sequence.takeIf { it > 0 })
    }

    private fun startHeartbeatJob(interval: Long) {
        heartbeatJob?.cancel()
        heartbeatJob = launch {
            delay(Random.nextLong(interval))
            while (isActive && !closed) {
                if (!heartbeatAcknowledged) {
                    logger.w("Gateway", "Heartbeat not acknowledged; reconnecting")
                    reconnectWebSocket()
                    return@launch
                }
                try {
                    sendHeartbeat()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logger.w("Gateway", "Heartbeat failed; reconnecting")
                    websocket?.cancel()
                    return@launch
                }
                delay(interval)
            }
        }
    }

    private suspend fun reconnectWebSocket() {
        websocket?.close(CloseReason(4000, "Reconnecting"))
    }

    private suspend fun sendIdentify() = send(IDENTIFY, token.toIdentifyPayload())

    private suspend fun sendResume() = send(RESUME, Resume(seq = sequence, sessionId = sessionId, token = token))

    private suspend inline fun <reified T> send(op: OpCode, d: T?) {
        val session = websocket ?: return
        if (session.isActive) session.send(Frame.Text(json.encodeToString(OutgoingPayload(op = op, d = d))))
    }

    private suspend fun publishLatestPresence() {
        presenceMutex.withLock {
            if (connected && !closed) latestPresence?.let { send(PRESENCE_UPDATE, it) }
        }
    }

    override suspend fun sendActivity(presence: Presence) {
        presenceMutex.withLock {
            if (closed) return
            latestPresence = presence
            if (connected) {
                try {
                    send(PRESENCE_UPDATE, presence)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logger.w("Gateway", "Presence update failed; reconnecting")
                    websocket?.cancel()
                }
            }
        }
    }

    override fun isWebSocketConnected(): Boolean = connected && websocket?.isActive == true

    override fun isRunning(): Boolean = !closed && connectionJob?.isActive == true

    private fun clearSession() {
        sequence = 0
        sessionId = null
        resumeGatewayUrl = null
    }

    override fun close() {
        synchronized(connectionLock) {
            if (closed) return
            closed = true
            connected = false
            latestPresence = null
            clearSession()
            heartbeatJob?.cancel()
            handshakeJob?.cancel()
            connectionJob?.cancel()
            websocket?.cancel()
            scopeJob.cancel()
            client.close()
        }
    }
}
