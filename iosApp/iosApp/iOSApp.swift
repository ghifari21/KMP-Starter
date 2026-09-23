import SwiftUI
import shared

@main
struct iOSApp: App {
    init() {
        // Initialize Koin once at startup with guard against double-init
        MainViewControllerKt.initApp()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}