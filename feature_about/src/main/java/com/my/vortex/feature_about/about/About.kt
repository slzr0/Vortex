/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * About.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.vortex.feature_about.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ContactSupport
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.my.vortex.feature_about.BuildConfig
import com.my.vortex.resources.R
import com.my.vortex.ui.components.BackButton
import com.my.vortex.ui.components.SettingItem

const val github_Repository = "https://github.com/dead8309/Kizzy"
const val github_Release = "$github_Repository/releases"
const val github_Issues = "$github_Repository/issues/new"
const val github_privacy_policy = "$github_Repository/blob/master/TERMS_OF_SERVICE.md"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun About(onBackPressed: () -> Unit, navigateToCredits: () -> Unit) {
    val uri = LocalUriHandler.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.about)) },
                navigationIcon = { BackButton(onClick = onBackPressed) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("${stringResource(R.string.version)} ${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(stringResource(R.string.about_upstream), style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            item { SettingItem(stringResource(R.string.privacy_policy), stringResource(R.string.privacy_policy_desc), Icons.Outlined.PrivacyTip) { uri.openUri(github_privacy_policy) } }
            item { SettingItem(stringResource(R.string.github_readme), stringResource(R.string.github_readme_desc), Icons.Outlined.Description) { uri.openUri(github_Repository) } }
            item { SettingItem(stringResource(R.string.github_latest_release), stringResource(R.string.upstream_releases_desc), Icons.Outlined.NewReleases) { uri.openUri(github_Release) } }
            item { SettingItem(stringResource(R.string.github_issue), stringResource(R.string.github_issue_desc), Icons.AutoMirrored.Outlined.ContactSupport) { uri.openUri(github_Issues) } }
            item { SettingItem(stringResource(R.string.credits), stringResource(R.string.credits_desc), Icons.Outlined.AutoAwesome, onClick = navigateToCredits) }
        }
    }
}
