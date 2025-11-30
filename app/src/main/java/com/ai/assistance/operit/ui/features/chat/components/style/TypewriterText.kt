package com.ai.assistance.operit.ui.features.chat.components.style

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay

@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    isVisible: Boolean = true,
    onAnimationComplete: () -> Unit = {}
) {
    var displayedText by remember { mutableStateOf("") }
    
    LaunchedEffect(text, isVisible) {
        if (isVisible) {
            // Reset if text changes significantly or just starting
            if (!text.startsWith(displayedText)) {
                displayedText = ""
            }
            
            // If we already showed everything, don't re-animate unless text grew
            if (displayedText.length < text.length) {
                val startIndex = displayedText.length
                for (i in startIndex until text.length) {
                    displayedText = text.substring(0, i + 1)
                    delay(20) // Adjust speed here (20ms per char)
                }
                onAnimationComplete()
            }
        }
    }

    Text(
        text = displayedText,
        modifier = modifier,
        color = color,
        style = style
    )
}
