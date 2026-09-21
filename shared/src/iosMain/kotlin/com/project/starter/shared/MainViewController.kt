package com.project.starter.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.project.starter.shared.di.initKoin
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initKoin()
    return ComposeUIViewController { App() }
}
