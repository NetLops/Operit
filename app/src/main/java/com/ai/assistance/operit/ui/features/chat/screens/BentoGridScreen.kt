package com.ai.assistance.operit.ui.features.chat.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ai.assistance.operit.ui.features.chat.components.style.MagicMotionEffect
import com.ai.assistance.operit.ui.features.chat.components.style.TypewriterText
import com.ai.assistance.operit.ui.features.chat.viewmodel.ChatViewModel

@Composable
fun BentoGridScreen(
    viewModel: ChatViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToInspiration: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Main Grid Area
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left: Large Recent Generated Card
            RecentGeneratedCard(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight(),
                viewModel = viewModel
            )

            // Right: Vertical Stack
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Inspiration Library
                BentoCard(
                    modifier = Modifier.weight(1f),
                    title = "Inspiration",
                    icon = Icons.Default.Lightbulb,
                    color = Color(0xFFFFF3E0), // Light Orange
                    onClick = onNavigateToInspiration
                )

                // History
                BentoCard(
                    modifier = Modifier.weight(1f),
                    title = "History",
                    icon = Icons.Default.History,
                    color = Color(0xFFE3F2FD), // Light Blue
                    onClick = onNavigateToHistory
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom: Text Input Area (Placeholder for now, will integrate ChatInputSection)
        // Note: The actual input section is handled by the parent screen's bottom bar,
        // but visually it completes the Bento grid.
        // We might want to customize the input section to match this style.
    }
}

@Composable
fun RecentGeneratedCard(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel
) {
    // Mock state for demonstration
    val isGenerating = false // Replace with viewModel.isGenerating
    val lastGeneratedContent = "Here is a generated image or text..." // Replace with viewModel.lastContent

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background / Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF5F5F5),
                                Color(0xFFE0E0E0)
                            )
                        )
                    )
            ) {
                if (isGenerating) {
                    MagicMotionEffect(modifier = Modifier.fillMaxSize())
                } else {
                    // Display Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Recent Generation",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Use Typewriter for text content
                        TypewriterText(
                            text = lastGeneratedContent,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = Color.Black.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(alpha = 0.8f)
            )
        }
    }
}
