package com.project.starter.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.project.starter.shared.di.initKoin
import org.koin.core.context.GlobalContext
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }

/**
 * Call this ONCE from iOSApp.swift at startup.
 * Guards against KoinAppAlreadyStartedException on repeated calls.
 */
fun initApp() {
    if (GlobalContext.getOrNull() == null) {
        initKoin()
    }
}
