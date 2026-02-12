package com.bowlingclub.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(
        route = Screen.Home.route,
        title = "홈",
        icon = Icons.Default.Home
    )
    object Tournament : BottomNavItem(
        route = Screen.TournamentList.route,
        title = "정기전",
        icon = Icons.Default.EmojiEvents
    )
    object Statistics : BottomNavItem(
        route = Screen.Statistics.route,
        title = "통계",
        icon = Icons.Default.BarChart
    )
    object Settings : BottomNavItem(
        route = Screen.Settings.route,
        title = "설정",
        icon = Icons.Default.Settings
    )

    companion object {
        val items = listOf(Home, Tournament, Statistics, Settings)
    }
}
