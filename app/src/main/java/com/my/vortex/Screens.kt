package com.my.vortex

import android.animation.ValueAnimator
import android.webkit.CookieManager
import android.webkit.WebStorage
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.my.vortex.feature_profile.ui.component.DiscordLoginWebView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private val Background = Color(0xFF090F14)
private val Foreground = Color(0xFFF4F8F3)
private val Subtle = Color(0xFFA4B6B4)
private val Accent = Color(0xFFC3F577)
private val Blue = Color(0xFF8AD0DC)
private val Card = Color(0xFF16242A)
private val Border = Color(0xFF31464A)
private val Discord = Color(0xFF5865F2)

@Composable
private fun Mark(modifier: Modifier = Modifier, rotation: Float = 0f) {
    Canvas(modifier) {
        val d = size.minDimension
        val x = size.width / 2
        val y = size.height / 2
        drawCircle(Accent.copy(alpha = .055f), d * .49f, Offset(x, y))
        drawCircle(Blue.copy(alpha = .18f), d * .48f, Offset(x, y), style = Stroke(d * .008f))
        rotate(rotation, Offset(x, y)) {
            drawArc(Accent, 205f, 250f, false, Offset(x - d * .30f, y - d * .30f), Size(d * .60f, d * .60f), style = Stroke(d * .065f, cap = StrokeCap.Round))
            drawArc(Blue, 25f, 230f, false, Offset(x - d * .19f, y - d * .19f), Size(d * .38f, d * .38f), style = Stroke(d * .05f, cap = StrokeCap.Round))
        }
        drawCircle(Accent, d * .035f, Offset(x, y))
    }
}

@Composable
internal fun LoadingScreen() {
    val rotation = if (ValueAnimator.areAnimatorsEnabled()) {
        val transition = rememberInfiniteTransition(label = "loading orbit")
        val value by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(3600, easing = LinearEasing), RepeatMode.Restart), label = "rotation")
        value
    } else 0f
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF1A3537), Background, Background))), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Mark(Modifier.size(168.dp), rotation)
            Text("VORTEX", color = Foreground, fontSize = 25.sp, fontWeight = FontWeight.Black, letterSpacing = 7.sp)
            Text("MAKE SPACE FOR YOUR PRESENCE", color = Subtle, fontSize = 10.sp, letterSpacing = 2.sp, textAlign = TextAlign.Center)
            Row(Modifier.padding(top = 25.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CircularProgressIndicator(Modifier.size(16.dp), color = Accent, strokeWidth = 2.dp)
                Text("Getting everything ready", color = Subtle, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun Page(step: Int, eyebrow: String, title: String, subtitle: String, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(primary = Accent, background = Background, surface = Card, onSurface = Foreground)) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF1A3437), Background, Background)))) {
            Column(
                Modifier.align(Alignment.TopCenter).widthIn(max = 600.dp).fillMaxSize().statusBarsPadding().navigationBarsPadding()
                    .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Mark(Modifier.size(40.dp))
                    Text("VORTEX", Modifier.padding(start = 10.dp), color = Foreground, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                    Spacer(Modifier.weight(1f))
                    Text("SETUP  0$step / 02", color = Subtle, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Box(Modifier.weight(1f).height(4.dp).background(Accent, CircleShape))
                    Box(Modifier.weight(1f).height(4.dp).background(if (step == 2) Accent else Border, CircleShape))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("01  ACCESS", color = if (step == 1) Accent else Subtle, fontSize = 10.sp, letterSpacing = 1.sp)
                    Text("02  DISCORD", color = if (step == 2) Accent else Subtle, fontSize = 10.sp, letterSpacing = 1.sp)
                }
                Spacer(Modifier.height(9.dp))
                Text(eyebrow, color = Accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
                Text(title, color = Foreground, fontSize = 35.sp, lineHeight = 41.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
                Text(subtitle, color = Subtle, fontSize = 15.sp, lineHeight = 23.sp)
                Spacer(Modifier.height(4.dp))
                content()
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun Panel(content: @Composable () -> Unit) {
    Surface(Modifier.fillMaxWidth().border(1.dp, Border, RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp), color = Card.copy(alpha = .95f)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { content() }
    }
}

@Composable
private fun Label(text: String, active: Boolean) {
    Surface(shape = CircleShape, color = if (active) Accent.copy(alpha = .13f) else Color(0xFF304044)) {
        Text(text, Modifier.padding(horizontal = 11.dp, vertical = 7.dp), color = if (active) Accent else Subtle,
            fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = .6.sp)
    }
}

@Composable
private fun Action(text: String, onClick: () -> Unit, color: Color = Accent) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp), shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = if (color == Discord) Color.White else Background)) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Outlined.ArrowForward, contentDescription = null)
    }
}

@Composable
private fun SecondaryAction(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(text, color = Foreground, fontWeight = FontWeight.Medium) }
}

@Composable
private fun AccessCard(icon: ImageVector, title: String, description: String, enabled: Boolean, optional: Boolean, action: String, onAction: () -> Unit) {
    Panel {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(14.dp), color = Blue.copy(alpha = .12f)) {
                Icon(icon, contentDescription = null, tint = Blue, modifier = Modifier.padding(10.dp).size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Foreground, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(if (optional) "OPTIONAL" else "SETUP REQUIREMENT", color = Subtle, fontSize = 10.sp, letterSpacing = .7.sp)
            }
            Label(if (enabled) "ON" else "OFF", enabled)
        }
        Text(description, color = Subtle, fontSize = 13.sp, lineHeight = 20.sp)
        TextButton(onClick = onAction, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(action, color = Accent, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = Accent)
        }
    }
}

@Composable
internal fun PermissionScreen(enabled: Boolean, asked: Boolean, usageEnabled: Boolean, batteryEnabled: Boolean,
    onAllow: () -> Unit, onSettings: () -> Unit, onUsageSettings: () -> Unit, onBatterySettings: () -> Unit,
    onContinue: () -> Unit, onBack: (() -> Unit)?) {
    Page(1, "YOUR SPACE, YOUR CHOICE", "Let’s get\nyou ready.", "A little setup now. You stay in control of every permission.") {
        AccessCard(Icons.Outlined.NotificationsActive, "Notifications", "Show a visible notification for future background activity. Vortex does not read your notifications.", enabled, false,
            if (enabled || asked || android.os.Build.VERSION.SDK_INT < 33) "Open notification settings" else "Allow notifications", if (enabled) onSettings else onAllow)
        if (!enabled && asked) Text("Notifications are off. Enable them in Android settings, then return here.", color = Accent, fontSize = 13.sp, lineHeight = 20.sp)
        AccessCard(Icons.Outlined.QueryStats, "Usage access", "Optional for possible future app detection. This version does not read or collect usage data.", usageEnabled, true,
            "Manage usage access", onUsageSettings)
        AccessCard(Icons.Outlined.BatteryChargingFull, "Battery settings", "Optional system setting for future background activity. Unrestricted use may increase battery consumption; this version has no background service.", batteryEnabled, true,
            "Review battery settings", onBatterySettings)
        Text("Optional access can be skipped or changed later. Android controls these settings, and device menus may differ.", color = Subtle, fontSize = 12.sp, lineHeight = 19.sp)
        if (enabled) Action(if (onBack == null) "Continue to Discord" else "Done", onContinue)
        if (onBack != null) SecondaryAction("Back", onBack)
    }
}

@Composable
internal fun SignInScreen(onToken: (String) -> Boolean) {
    var showLogin by remember { mutableStateOf(false) }
    var verifying by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    var webError by remember { mutableStateOf(false) }
    var webProgress by remember { mutableIntStateOf(0) }
    var attempt by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    Page(2, "YOUR ACCOUNT", "Connect to\nDiscord.", "One last step to make this space yours.") {
        Panel {
            Surface(shape = RoundedCornerShape(14.dp), color = Discord.copy(alpha = .22f)) {
                Icon(Icons.Outlined.Shield, contentDescription = null, tint = Color(0xFFBAC0FF), modifier = Modifier.padding(12.dp).size(24.dp))
            }
            Text("Know before you connect", color = Foreground, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Text("This unofficial in-app login extracts a Discord user token. It may stop working or violate Discord’s terms. Vortex encrypts the saved token; WebView also keeps its own site data until sign-out.", color = Subtle, fontSize = 14.sp, lineHeight = 21.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = Accent, modifier = Modifier.size(15.dp))
                Text("LOCAL ENCRYPTED STORAGE", color = Accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = .6.sp)
            }
        }
        if (error) {
            Text("Couldn’t verify or save your session. Check your connection, then try again.", color = Color(0xFFFFB6AD), fontSize = 13.sp, lineHeight = 20.sp)
            SecondaryAction("Clear Discord web session", {
                CookieManager.getInstance().removeAllCookies { CookieManager.getInstance().flush() }
                WebStorage.getInstance().deleteAllData()
                error = false
            })
        }
        if (verifying) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator(Modifier.size(20.dp), color = Accent, strokeWidth = 2.dp)
                Text("Checking your session with Discord…", color = Subtle, fontSize = 14.sp)
            }
        } else Action("Sign in with Discord", { error = false; webError = false; webProgress = 0; attempt++; showLogin = true }, Discord)
        Text("Sign out any time to remove your local session.", color = Subtle, fontSize = 12.sp, lineHeight = 19.sp)
    }
    if (showLogin) {
        BackHandler { showLogin = false }
        Dialog(onDismissRequest = { showLogin = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(), color = Background) {
                Column {
                    Row(Modifier.fillMaxWidth().heightIn(min = 60.dp).padding(start = 12.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showLogin = false }) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Close sign in", tint = Foreground) }
                        Column(Modifier.weight(1f)) {
                            Text("Discord sign in", color = Foreground, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text("Discord login", color = Subtle, fontSize = 12.sp)
                        }
                        Icon(Icons.Outlined.Lock, contentDescription = "Discord sign-in", tint = Accent, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(12.dp))
                    }
                    if (webProgress in 1..99 && !webError) LinearProgressIndicator(progress = { webProgress / 100f }, modifier = Modifier.fillMaxWidth(), color = Accent, trackColor = Border)
                    Box(Modifier.weight(1f)) {
                        key(attempt) {
                            DiscordLoginWebView(onLoginCompleted = { token ->
                                if (!verifying) {
                                    verifying = true
                                    showLogin = false
                                    scope.launch {
                                        try {
                                            if (!verifyDiscordToken(token) || !onToken(token)) error = true
                                        } catch (e: CancellationException) { throw e }
                                        catch (_: Exception) { error = true }
                                        finally { verifying = false }
                                    }
                                }
                            }, onProgress = { webProgress = it }, onError = { webError = true })
                        }
                        if (webError) {
                            Column(Modifier.fillMaxSize().background(Background).padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Couldn’t load Discord", color = Foreground, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                                Text("Check your internet connection and try again.", Modifier.padding(top = 10.dp, bottom = 20.dp), color = Subtle, textAlign = TextAlign.Center)
                                Action("Try again", { webError = false; webProgress = 0; attempt++ })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun CompleteScreen(onManageAccess: () -> Unit, onSignOut: () -> Unit) {
    val reveal = remember { Animatable(if (ValueAnimator.areAnimatorsEnabled()) 0f else 1f) }
    LaunchedEffect(Unit) { if (ValueAnimator.areAnimatorsEnabled()) reveal.animateTo(1f, tween(650, easing = FastOutSlowInEasing)) }
    Page(2, "SETUP COMPLETE", "You’re all\nset.", "Your Discord sign-in is saved on this device.") {
        Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.scale(.75f + .25f * reveal.value).alpha(reveal.value).size(134.dp).background(Accent.copy(alpha = .10f), CircleShape).border(1.dp, Accent.copy(alpha = .3f), CircleShape), contentAlignment = Alignment.Center) {
                Mark(Modifier.size(106.dp))
                Box(Modifier.align(Alignment.BottomEnd).size(38.dp).background(Accent, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Check, contentDescription = "Setup complete", tint = Background, modifier = Modifier.size(22.dp))
                }
            }
        }
        Panel {
            Text("A fresh start", color = Foreground, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text("Loading, access and sign-in are ready. More of your Vortex space will come later.", color = Subtle, fontSize = 14.sp, lineHeight = 22.sp)
            Label("SESSION SAVED", true)
        }
        Action("Manage access", onManageAccess)
        SecondaryAction("Sign out", onSignOut)
        Text("Signing out removes the local session but does not revoke it on Discord.", color = Subtle, fontSize = 12.sp, lineHeight = 19.sp)
    }
}
