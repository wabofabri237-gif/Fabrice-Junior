package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.analytics.AnalyticsManager
import com.example.ui.navigation.AfriMarketNavGraph
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AnalyticsManager.initialize(this)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        AfriMarketNavGraph()
      }
    }
  }
}
