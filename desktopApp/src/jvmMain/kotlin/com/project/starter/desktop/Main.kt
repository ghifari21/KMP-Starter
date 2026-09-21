package com.project.starter.desktop

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.project.starter.shared.App
import com.project.starter.shared.di.initKoin

fun main() {
    initKoin()
    application {
        val windowState =
            rememberWindowState(
                position = WindowPosition(Alignment.Center),
                width = 1200.dp,
                height = 800.dp,
            )
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "KMP Starter",
        ) {
            App()
        }
    }
}
