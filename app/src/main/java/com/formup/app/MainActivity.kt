// Starts the app and handles notification links and screen settings.

package com.formup.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.formup.app.ui.app.FormUpApp
import com.formup.app.ui.theme.FormUpColors
import com.formup.app.ui.theme.FormUpTheme

class MainActivity : AppCompatActivity() {

    private val notificationIntent = mutableStateOf<Intent?>(null)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = getSharedPreferences("formup_settings", MODE_PRIVATE)
        notificationIntent.value = intent

        setContent {

            var darkTheme by remember {
                mutableStateOf(preferences.getBoolean("dark_theme", true))
            }

            FormUpTheme(darkTheme = darkTheme) {

                val view = LocalView.current

                SideEffect {
                    window.statusBarColor = FormUpColors.Background.toArgb()
                    window.navigationBarColor = FormUpColors.Background.toArgb()

                    val controller = WindowCompat.getInsetsController(window, view)

                    controller.isAppearanceLightStatusBars = !darkTheme
                    controller.isAppearanceLightNavigationBars = !darkTheme
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = FormUpColors.Background
                ) {
                    FormUpApp(
                        darkTheme = darkTheme,
                        onDarkThemeChange = { enabled ->

                            darkTheme = enabled

                            preferences.edit()
                                .putBoolean("dark_theme", enabled)
                                .apply()
                        },
                        notificationIntent = notificationIntent.value,
                        onNotificationIntentHandled = {
                            notificationIntent.value = null
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.d("MainActivity", "onNewIntent")
        notificationIntent.value = intent
    }
}
