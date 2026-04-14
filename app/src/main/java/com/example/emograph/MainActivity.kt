package com.example.emograph

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.emograph.navigation.EmoGraphNavGraph
import com.example.emograph.ui.theme.EmoGraphTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EmoGraphTheme {
                EmoGraphNavGraph()
            }
        }
    }
}
