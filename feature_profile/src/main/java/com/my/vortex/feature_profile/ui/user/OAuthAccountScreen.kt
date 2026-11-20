package com.my.vortex.feature_profile.ui.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.my.vortex.domain.model.user.User
import com.my.vortex.resources.R
import com.my.vortex.ui.components.BackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OAuthAccountScreen(user: User, onBackPressed: () -> Unit, onLogout: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(R.string.account)) },
            navigationIcon = { BackButton(onClick = onBackPressed) })
    }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = user.avatar?.let { user.getAvatarImage() },
                        contentDescription = null,
                        placeholder = painterResource(R.drawable.error_avatar),
                        error = painterResource(R.drawable.error_avatar),
                        fallback = painterResource(R.drawable.error_avatar),
                        modifier = Modifier.size(80.dp).clip(CircleShape)
                    )
                    Text(stringResource(R.string.oauth_signed_in_as), style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(user.username.orEmpty(), style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center)
                }
            }
            OutlinedButton(onClick = onLogout) { Text(stringResource(R.string.logout)) }
        }
    }
}
