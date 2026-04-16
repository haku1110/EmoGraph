package com.kha98.emograph

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kha98.emograph.data.repository.EmotionRepository
import com.kha98.emograph.navigation.EmoGraphNavGraph
import com.kha98.emograph.ui.theme.EmoGraphTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmotionRepository.init(this)
        enableEdgeToEdge()
        setContent {
            EmoGraphTheme {
                EmoGraphNavGraph()
            }
        }
    }
}
