package com.bowlingclub.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val BowlingShapes = Shapes(
    // Small - Buttons, chips, small cards
    small = RoundedCornerShape(8.dp),

    // Medium - Standard cards, dialogs
    medium = RoundedCornerShape(12.dp),

    // Large - Bottom sheets, large dialogs
    large = RoundedCornerShape(16.dp)
)

// Custom shapes for specific components
object BowlingCustomShapes {
    // Score card frame
    val ScoreFrame = RoundedCornerShape(4.dp)

    // Floating action button
    val FAB = RoundedCornerShape(16.dp)

    // Member avatar
    val Avatar = RoundedCornerShape(50)  // Fully rounded

    // Badge (rank, status indicator)
    val Badge = RoundedCornerShape(12.dp)

    // Input field
    val TextField = RoundedCornerShape(8.dp)

    // Score input button (in bowling frame)
    val ScoreButton = RoundedCornerShape(6.dp)

    // Statistics card
    val StatCard = RoundedCornerShape(16.dp)

    // Navigation drawer
    val Drawer = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 16.dp,
        bottomEnd = 16.dp,
        bottomStart = 0.dp
    )

    // Bottom navigation bar
    val BottomBar = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    // Tournament bracket card
    val BracketCard = RoundedCornerShape(
        topStart = 12.dp,
        topEnd = 12.dp,
        bottomStart = 12.dp,
        bottomEnd = 12.dp
    )
}
