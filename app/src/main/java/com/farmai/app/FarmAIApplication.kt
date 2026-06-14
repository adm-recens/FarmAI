package com.farmai.app

import android.app.Application
import com.farmai.app.data.SampleDataSeeder
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FarmAIApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SampleDataSeeder.seed(this)
    }
}