package com.my.vortex

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.my.vortex.feature_profile.getUserInfo
import com.my.vortex.feature_profile.ui.component.DiscordLoginWebView
import com.my.vortex.preference.Prefs
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

internal val Night = Color(0xFF0C1112)
internal val White = Color(0xFFF1F5EF)
internal val Mist = Color(0xFF9BAAA4)
internal val Lime = Color(0xFFC2F76B)
private val Panel = Color(0xFF192221)
private val Line = Color(0xFF34423C)

@Composable
internal fun VortexEmblem(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.minDimension
        val center = Offset(size.width / 2, size.height / 2)
        drawCircle(color = Lime.copy(alpha = .07f), radius = w * .48f, center = center)
        drawCircle(color = Lime.copy(alpha = .18f), radius = w * .46f, center = center, style = Stroke(w * .012f))
        drawArc(Lime, 205f, 250f, false, topLeft = Offset(size.width / 2 - w * .30f, size.height / 2 - w * .30f),
            size = androidx.compose.ui.geometry.Size(w * .60f, w * .60f), style = Stroke(w * .075f, cap = StrokeCap.Round))
        drawArc(Lime.copy(alpha = .55f), 25f, 235f, false, topLeft = Offset(size.width / 2 - w * .19f, size.height / 2 - w * .19f),
            size = androidx.compose.ui.geometry.Size(w * .38f, w * .38f), style = Stroke(w * .055f, cap = StrokeCap.Round))
        drawCircle(color = Lime, radius = w * .035f, center = center)
    }
}

@Composable
internal fun SetupShell(step: Int, eyebrow: String, title: String, description: String, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(primary = Lime, background = Night, surface = Panel, onSurface = White)) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF172320), Night, Night)))) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    VortexEmblem(Modifier.size(42.dp))
                    Text("VORTEX", Modifier.padding(start = 10.dp), color = White, fontSize = 15.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                    Spacer(Modifier.weight(1f))
                    Text("SETUP  0$step / 02", color = Mist, fontSize = 11.sp, letterSpacing = 1.sp)
                }
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.weight(1f).height(3.dp).background(Lime, CircleShape))
                    Box(Modifier.weight(1f).height(3.dp).background(if (step == 2) Lime else Line, CircleShape))
                }
                Spacer(Modifier.height(12.dp))
                Text(eyebrow, color = Lime, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
                Text(title, color = White, fontSize = 36.sp, lineHeight = 41.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
                Text(description, color = Mist, fontSize = 15.sp, lineHeight = 23.sp)
                Spacer(Modifier.height(2.dp))
                content()
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
internal fun SetupCard(content: @Composable () -> Unit) {
    Surface(Modifier.fillMaxWidth().border(1.dp, Line, RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp), color = Panel) {
        Box(Modifier.padding(22.dp)) { content() }
    }
}

@Composable
internal fun StatusPill(text: String, enabled: Boolean) {
    Surface(shape = CircleShape, color = if (enabled) Lime.copy(alpha = .13f) else Color(0xFF303A35)) {
        Text(text, Modifier.padding(horizontal = 11.dp, vertical = 7.dp), color = if (enabled) Lime else Mist,
            fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp)
    }
}

@Composable
internal fun PrimaryAction(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().height(58.dp),
        shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = Night)) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Outlined.ArrowForward, contentDescription = null)
    }
}

@Composable
internal fun SecondaryAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier) { Text(text, color = Mist) }
}

@Composable
internal fun SignInScreen(onCompleted: () -> Unit) {
    var showLogin by remember { mutableStateOf(false) }
    var verifying by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    SetupShell(step = 2, eyebrow = "ONE LAST THING", title = "Make the\nconnection.",
        description = "Sign in to Discord to finish setting up your Vortex account.") {
        SetupCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Icon(Icons.Outlined.Shield, contentDescription = null, tint = Lime)
                Text("Your account, your call", color = White, fontSize = 20.sp)
                Text("Vortex uses an unofficial in-app Discord login. Your user token is stored in app-private storage without encryption. This method may stop working or violate Discord’s terms.", color = Mist, lineHeight = 22.sp)
            }
        }
        if (error) Text("Couldn’t verify your Discord session. Check your connection and try again.", color = Color(0xFFFFB4A9), fontSize = 13.sp, lineHeight = 19.sp)
        if (verifying) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator(Modifier.size(20.dp), color = Lime, strokeWidth = 2.dp)
                Text("Verifying your session…", color = Mist, fontSize = 14.sp)
            }
        } else {
            PrimaryAction("Sign in with Discord", { error = false; showLogin = true })
        }
        Text("You can sign out of this device at any time.", color = Mist, fontSize = 13.sp)
    }
    if (showLogin) {
        Dialog(onDismissRequest = { showLogin = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(), color = Night) {
                Column {
                    Row(Modifier.fillMaxWidth().padding(start = 18.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Discord sign in", Modifier.weight(1f), color = White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = { showLogin = false }) { Icon(Icons.Outlined.Close, contentDescription = "Close sign in", tint = White) }
                    }
                    DiscordLoginWebView { token ->
                        if (!verifying) {
                            verifying = true
                            showLogin = false
                            scope.launch {
                                try {
                                    if (token.isNotBlank() && getUserInfo(token)) {
                                        Prefs[Prefs.TOKEN] = token
                                        onCompleted()
                                    } else error = true
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    error = true
                                } finally {
                                    verifying = false
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ConnectedScreen(onSignOut: () -> Unit) {
    SetupShell(step = 2, eyebrow = "SETUP COMPLETE", title = "You’re in.",
        description = "Your Discord sign-in is saved on this device.") {
        SetupCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Lime)
                Text("All set", color = White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Text("Loading, permissions, and sign-in are ready. The rest of your Vortex space will come next.", color = Mist, lineHeight = 22.sp)
                StatusPill("SESSION SAVED", true)
            }
        }
        SecondaryAction("Sign out", onSignOut, Modifier.fillMaxWidth())
        Text("Signing out removes the local session but does not revoke it on Discord.", color = Mist, fontSize = 13.sp, lineHeight = 20.sp)
    }
}
