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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Security
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.my.vortex.feature_profile.ui.component.DiscordLoginWebView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private val Background = Color(0xFF0B1015)
private val Foreground = Color(0xFFF1F7F2)
private val Subtle = Color(0xFF9CAEB0)
private val Accent = Color(0xFFB5F270)
private val Blue = Color(0xFF72AFC4)
private val Card = Color(0xFF152126)
private val Border = Color(0xFF304244)

@Composable
private fun Mark(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val d = size.minDimension
        val x = size.width / 2
        val y = size.height / 2
        drawCircle(Accent.copy(alpha = .07f), d * .49f, Offset(x, y))
        drawCircle(Accent.copy(alpha = .18f), d * .48f, Offset(x, y), style = Stroke(d * .01f))
        drawArc(Accent, 205f, 250f, false, Offset(x - d * .30f, y - d * .30f), Size(d * .60f, d * .60f), style = Stroke(d * .07f, cap = StrokeCap.Round))
        drawArc(Blue, 25f, 230f, false, Offset(x - d * .19f, y - d * .19f), Size(d * .38f, d * .38f), style = Stroke(d * .05f, cap = StrokeCap.Round))
        drawCircle(Accent, d * .035f, Offset(x, y))
    }
}

@Composable
internal fun LoadingScreen() {
    Box(Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Mark(Modifier.size(132.dp))
            Text("VORTEX", color = Foreground, fontSize = 23.sp, fontWeight = FontWeight.Black, letterSpacing = 6.sp)
            Text("A SPACE OF YOUR OWN", color = Subtle, fontSize = 10.sp, letterSpacing = 2.sp)
            CircularProgressIndicator(Modifier.padding(top = 18.dp).size(20.dp), color = Accent, strokeWidth = 2.dp)
        }
    }
}

@Composable
private fun Page(step: Int, eyebrow: String, title: String, subtitle: String, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(primary = Accent, background = Background, surface = Card, onSurface = Foreground)) {
        Column(
            Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF172D30), Background, Background)))
                .statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(17.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Mark(Modifier.size(42.dp))
                Text("VORTEX", Modifier.padding(start = 10.dp), color = Foreground, fontSize = 15.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                Spacer(Modifier.weight(1f))
                Text("0$step / 02", color = Subtle, fontSize = 12.sp, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Box(Modifier.weight(1f).height(3.dp).background(Accent, CircleShape))
                Box(Modifier.weight(1f).height(3.dp).background(if (step == 2) Accent else Border, CircleShape))
            }
            Spacer(Modifier.height(14.dp))
            Text(eyebrow, color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
            Text(title, color = Foreground, fontSize = 35.sp, lineHeight = 41.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
            Text(subtitle, color = Subtle, fontSize = 15.sp, lineHeight = 23.sp)
            Spacer(Modifier.height(2.dp))
            content()
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun Panel(content: @Composable () -> Unit) {
    Surface(Modifier.fillMaxWidth().border(1.dp, Border, RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp), color = Card) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) { content() }
    }
}

@Composable
private fun Label(text: String, active: Boolean) {
    Surface(shape = CircleShape, color = if (active) Accent.copy(alpha = .12f) else Border) {
        Text(text, Modifier.padding(horizontal = 11.dp, vertical = 7.dp), color = if (active) Accent else Subtle,
            fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Composable
private fun Action(text: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Background)) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Outlined.ArrowForward, contentDescription = null)
    }
}

@Composable
private fun SecondaryAction(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(text, color = Subtle) }
}

@Composable
internal fun PermissionScreen(enabled: Boolean, asked: Boolean, onAllow: () -> Unit, onSettings: () -> Unit, onContinue: () -> Unit) {
    Page(1, "FIRST, THE BASICS", "Stay in\nthe loop.", "Let Vortex show notifications so future background activity stays visible and under your control.") {
        Panel {
            Icon(Icons.Outlined.NotificationsActive, contentDescription = null, tint = Accent, modifier = Modifier.size(28.dp))
            Text("Notification access", color = Foreground, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text(if (enabled) "Notifications are enabled on this device." else "Turn on notifications to continue setup. You can change this later in Android settings.", color = Subtle, lineHeight = 22.sp)
            Label(if (enabled) "ENABLED" else "NOT ENABLED", enabled)
        }
        Text("Vortex does not read your notifications. Background activity is not available in this first version.", color = Subtle, fontSize = 13.sp, lineHeight = 20.sp)
        if (enabled) Action("Continue", onContinue) else {
            if (asked) Text("Notifications are still off. Turn them on in Android settings and return to Vortex.", color = Accent, fontSize = 13.sp, lineHeight = 19.sp)
            Action(if (asked || android.os.Build.VERSION.SDK_INT < 33) "Open notification settings" else "Allow notifications", onAllow)
            if (!asked && android.os.Build.VERSION.SDK_INT >= 33) SecondaryAction("Open settings instead", onSettings)
        }
    }
}

@Composable
internal fun SignInScreen(onToken: (String) -> Boolean) {
    var showLogin by remember { mutableStateOf(false) }
    var verifying by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Page(2, "NEXT UP", "Connect your\nDiscord.", "Sign in to finish setting up your Vortex account.") {
        Panel {
            Icon(Icons.Outlined.Security, contentDescription = null, tint = Accent, modifier = Modifier.size(28.dp))
            Text("Before you continue", color = Foreground, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text("This unofficial in-app login extracts a Discord user token and stores it encrypted on this device. It may stop working or violate Discord’s terms.", color = Subtle, lineHeight = 22.sp)
        }
        if (error) Text("Could not verify or save your session. Check your connection and try again.", color = Color(0xFFFFB4A9), fontSize = 13.sp, lineHeight = 20.sp)
        if (verifying) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator(Modifier.size(20.dp), color = Accent, strokeWidth = 2.dp)
                Text("Verifying your session…", color = Subtle, fontSize = 14.sp)
            }
        } else Action("Sign in with Discord") { error = false; showLogin = true }
        Text("You can remove the saved session from this device at any time.", color = Subtle, fontSize = 13.sp, lineHeight = 20.sp)
    }
    if (showLogin) {
        Dialog(onDismissRequest = { showLogin = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(), color = Background) {
                Column {
                    Row(Modifier.fillMaxWidth().padding(start = 18.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Discord sign in", Modifier.weight(1f), color = Foreground, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = { showLogin = false }) { Icon(Icons.Outlined.Close, contentDescription = "Close sign in", tint = Foreground) }
                    }
                    DiscordLoginWebView { token ->
                        if (!verifying) {
                            verifying = true
                            showLogin = false
                            scope.launch {
                                try {
                                    if (!verifyDiscordToken(token) || !onToken(token)) error = true
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (_: Exception) {
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
internal fun CompleteScreen(onSignOut: () -> Unit) {
    Page(2, "SETUP COMPLETE", "You’re in.", "Your Discord sign-in is saved on this device.") {
        Panel {
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Accent, modifier = Modifier.size(28.dp))
            Text("Ready for what’s next", color = Foreground, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text("This first version includes setup and sign-in. More of your Vortex space will come later.", color = Subtle, lineHeight = 22.sp)
            Label("SESSION SAVED", true)
        }
        SecondaryAction("Sign out", onSignOut)
        Text("Signing out removes the local session but does not revoke it on Discord.", color = Subtle, fontSize = 13.sp, lineHeight = 20.sp)
    }
}
