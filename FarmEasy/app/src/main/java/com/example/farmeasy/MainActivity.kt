package com.example.farmeasy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.farmeasy.navigation.FarmEasyNavGraph
import com.example.farmeasy.ui.theme.FarmEasyTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as FarmEasyApplication
        val container = app.container

        setContent {
            val preferences by container.preferencesRepository.getPreferences().collectAsStateWithLifecycle(
                initialValue = com.example.farmeasy.domain.model.UserPreferences()
            )
            val useDarkTheme = if (preferences.isDarkMode) true else isSystemInDarkTheme()

            FarmEasyTheme(darkTheme = useDarkTheme) {
                FarmEasyNavGraph(appContainer = container)
            }
        }
    }
}