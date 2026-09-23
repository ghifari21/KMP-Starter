package com.project.starter.feat.auth.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.project.starter.core.designsystem.theme.AppTheme

@Composable
fun AuthScreen(onLoginClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.dimens.paddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Welcome",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(AppTheme.dimens.large))
            Text(
                text = "Please sign in to continue",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(AppTheme.dimens.extraLarge))
            Button(onClick = onLoginClick) {
                Text("Sign In (Demo)")
            }
        }
    }
}
