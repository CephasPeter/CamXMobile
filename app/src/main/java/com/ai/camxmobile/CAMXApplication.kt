package com.ai.camxmobile

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CAMXApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}