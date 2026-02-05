package com.chessassistant

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main Application class for Chess Assistant.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class ChessAssistantApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Application-level initialization can go here
    }
}
