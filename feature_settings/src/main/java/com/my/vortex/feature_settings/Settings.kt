package com.my.vortex.feature_settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.my.vortex.domain.model.user.User
import com.my.vortex.resources.R
import com.my.vortex.ui.components.Subtitle
import com.my.vortex.ui.components.chips

/** Settings destinations are shared by the full-screen hub and the compact home drawer. */
@Composable
fun SettingsDrawer(
    user: User?,
    showVortexQuickieRequestItem: Boolean,
    onRequestAddTile: () -> Unit,
    navigateToProfile: () -> Unit,
    navigateToStyleAndAppearance: () -> Unit,
    navigateToLanguages: () -> Unit,
    navigateToAbout: () -> Unit,
    navigateToRpcSettings: () -> Unit,
    navigateToLogsScreen: () -> Unit,
    modifier: Modifier = Modifier.width(300.dp),
    navigateToSetup: (() -> Unit)? = null,
) {
    val uriHandler = LocalUriHandler.current
    Surface(modifier = modifier.fillMaxHeight()) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                if (user != null) {
                    ProfileCardSmall(user, navigateToProfile)
                } else {
                    SettingsItemCard(stringResource(R.string.login_with_discord), Icons.Outlined.Person,
                        onClick = navigateToProfile)
                }
            }
            item { Subtitle(text = stringResource(R.string.general_settings)) }
            if (navigateToSetup != null) {
                item { SettingsItemCard(stringResource(R.string.setup), Icons.Outlined.Settings, onClick = navigateToSetup) }
            }
            item { SettingsItemCard(stringResource(R.string.display), Icons.Outlined.Palette, onClick = navigateToStyleAndAppearance) }
            item { SettingsItemCard(stringResource(R.string.language), Icons.Outlined.Language, onClick = navigateToLanguages) }
            item { SettingsItemCard(stringResource(R.string.settings), Icons.Outlined.Settings, onClick = navigateToRpcSettings) }
            item { SettingsItemCard(stringResource(R.string.logs), Icons.Outlined.BugReport, onClick = navigateToLogsScreen) }
            item { RequestQsTile(showVortexQuickieRequestItem, onRequestAddTile) }
            item { Subtitle(text = stringResource(R.string.drawer_subtitle_help)) }
            item { SettingsItemCard(stringResource(R.string.drawer_faq), Icons.AutoMirrored.Rounded.HelpOutline) { uriHandler.openUri("https://kizzy.vercel.app/#FAQ") } }
            item { SettingsItemCard("Discord", ImageVector.vectorResource(R.drawable.ic_discord)) { uriHandler.openUri(chips.first().url) } }
            item { SettingsItemCard(stringResource(R.string.about), Icons.Outlined.Info, onClick = navigateToAbout) }
            item {
                Text("${stringResource(R.string.app_name)} · ${BuildConfig.VERSION_NAME}",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun RequestQsTile(
    visible: Boolean = true,
    onRequestAddTile: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        exit = fadeOut(tween(800))
    ) {
        SettingsItemCard(
            title = stringResource(R.string.qs_tile_label),
            icon = Icons.Outlined.Star,
            selected = true,
        ) {
            onRequestAddTile()
        }
    }
}


@Composable
fun SettingsItemCard(
    title: String,
    icon: ImageVector,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    NavigationDrawerItem(
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(
                text = title,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier
                    .padding(end = 5.dp)
                    .size(28.dp)
            )
        },
        selected = selected,
        onClick = { onClick() }
    )
}

@Composable
fun ProfileCardSmall(
    user: User?,
    navigateToProfile: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp)),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        onClick = navigateToProfile
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                AsyncImage(
                    model = user?.getAvatarImage(),
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(50.dp)
                        .clip(CircleShape),
                    contentDescription = user?.username,
                    error = painterResource(R.drawable.error_avatar),
                    placeholder = painterResource(R.drawable.error_avatar),
                )
                Text(
                    buildAnnotatedString {
                        withStyle(
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Left
                            ).toSpanStyle()
                        ) {
                            append((user?.globalName ?: user?.username) + "\r\n")
                        }
                        withStyle(
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                textAlign = TextAlign.Left
                            ).toSpanStyle()
                        ) {
                            append(user?.username)
                            if (user?.discriminator != "0")
                                append("#${user?.discriminator}")
                        }
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun SettingsDrawerPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {
        SettingsDrawer(
            showVortexQuickieRequestItem = false,
            onRequestAddTile = {},
            user = null,
            navigateToProfile = {},
            navigateToStyleAndAppearance = {},
            navigateToLanguages = {},
            navigateToAbout = {},
            navigateToRpcSettings = {},
            navigateToLogsScreen = {}
        )
    }
}