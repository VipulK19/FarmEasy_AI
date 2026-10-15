package com.example.farmeasy

import android.app.Application
import com.example.farmeasy.data.repository.FarmRepositoryImpl
import com.example.farmeasy.data.repository.NotificationRepositoryImpl
import com.example.farmeasy.di.AppContainer
import com.example.farmeasy.di.DefaultAppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FarmEasyApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)

        // Seed initial offline sample datasets
        applicationScope.launch {
            try {
                (container.farmRepository as? FarmRepositoryImpl)?.seedSampleDataIfEmpty()
                (container.notificationRepository as? NotificationRepositoryImpl)?.seedSampleIfEmpty()
            } catch (_: Exception) {}
        }
    }
}
