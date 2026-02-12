package com.bowlingclub.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bowlingclub.app.ui.home.HomeScreen
import com.bowlingclub.app.ui.member.MemberDetailScreen
import com.bowlingclub.app.ui.member.MemberEditScreen
import com.bowlingclub.app.ui.member.MemberListScreen
import com.bowlingclub.app.ui.score.ManualScoreInputScreen
import com.bowlingclub.app.ui.score.ScoreInputTabScreen
import com.bowlingclub.app.ui.settings.PinLockScreen
import com.bowlingclub.app.ui.settings.SettingsScreen
import com.bowlingclub.app.ui.statistics.StatisticsScreen
import com.bowlingclub.app.ui.tournament.ParticipantCheckScreen
import com.bowlingclub.app.ui.tournament.TournamentCreateScreen
import com.bowlingclub.app.ui.tournament.TournamentDetailScreen
import com.bowlingclub.app.ui.tournament.TournamentListScreen
import com.bowlingclub.app.ui.team.TeamAssignScreen
import com.bowlingclub.app.ui.team.TeamResultScreen
import com.bowlingclub.app.viewmodel.PinLockMode

@Composable
fun BowlingNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToMembers = {
                    navController.navigate(Screen.MemberList.route)
                },
                onNavigateToTournaments = {
                    navController.navigate(Screen.TournamentList.route)
                },
                onNavigateToTournamentDetail = { tournamentId ->
                    navController.navigate(Screen.TournamentDetail.createRoute(tournamentId))
                },
                onNavigateToTournamentCreate = {
                    navController.navigate(Screen.TournamentCreate.route)
                }
            )
        }

        composable(Screen.MemberList.route) {
            MemberListScreen(
                onMemberClick = { memberId ->
                    navController.navigate(Screen.MemberDetail.createRoute(memberId))
                },
                onAddMemberClick = {
                    navController.navigate(Screen.MemberEdit.createRoute())
                }
            )
        }

        composable(
            route = Screen.MemberDetail.route,
            arguments = listOf(
                navArgument("memberId") { type = NavType.IntType }
            )
        ) {
            MemberDetailScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToEdit = { memberId ->
                    navController.navigate(Screen.MemberEdit.createRoute(memberId))
                }
            )
        }

        composable(
            route = Screen.MemberEdit.route,
            arguments = listOf(
                navArgument("memberId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) {
            MemberEditScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.TournamentList.route) {
            TournamentListScreen(
                onTournamentClick = { tournamentId ->
                    navController.navigate(Screen.TournamentDetail.createRoute(tournamentId))
                },
                onCreateClick = {
                    navController.navigate(Screen.TournamentCreate.route)
                }
            )
        }

        composable(Screen.TournamentCreate.route) {
            TournamentCreateScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToParticipants = { tournamentId ->
                    // TournamentCreate를 백스택에서 제거
                    navController.popBackStack(Screen.TournamentCreate.route, inclusive = true)
                    // 정기전 목록이 백스택에 없으면 추가 (홈에서 직접 온 경우)
                    navController.navigate(Screen.TournamentList.route) {
                        launchSingleTop = true
                    }
                    // ParticipantCheck으로 이동 → 저장 후 popBackStack 시 정기전 목록으로
                    navController.navigate(Screen.ParticipantCheck.createRoute(tournamentId))
                }
            )
        }

        composable(
            route = Screen.TournamentDetail.route,
            arguments = listOf(
                navArgument("tournamentId") { type = NavType.IntType }
            )
        ) {
            TournamentDetailScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToParticipants = { tournamentId ->
                    navController.navigate(Screen.ParticipantCheck.createRoute(tournamentId))
                },
                onNavigateToScoreInput = { tournamentId ->
                    navController.navigate(Screen.ScoreInput.createRoute(tournamentId))
                },
                onNavigateToTeamAssign = { tournamentId ->
                    navController.navigate(Screen.TeamAssign.createRoute(tournamentId))
                },
                onNavigateToTeamResult = { tournamentId ->
                    navController.navigate(Screen.TeamResult.createRoute(tournamentId))
                }
            )
        }

        composable(
            route = Screen.ParticipantCheck.route,
            arguments = listOf(
                navArgument("tournamentId") { type = NavType.IntType }
            )
        ) {
            ParticipantCheckScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ScoreInput.route,
            arguments = listOf(
                navArgument("tournamentId") { type = NavType.IntType }
            )
        ) {
            ScoreInputTabScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.TeamAssign.route,
            arguments = listOf(
                navArgument("tournamentId") { type = NavType.IntType }
            )
        ) {
            TeamAssignScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.TeamResult.route,
            arguments = listOf(
                navArgument("tournamentId") { type = NavType.IntType }
            )
        ) {
            TeamResultScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToPinSetup = { mode ->
                    navController.navigate(Screen.PinLock.createRoute(mode))
                }
            )
        }

        composable(
            route = Screen.PinLock.route,
            arguments = listOf(
                navArgument("mode") {
                    type = NavType.StringType
                    defaultValue = "SETUP"
                }
            )
        ) { backStackEntry ->
            val modeString = backStackEntry.arguments?.getString("mode") ?: "SETUP"
            val mode = try { PinLockMode.valueOf(modeString) } catch (e: Exception) { PinLockMode.SETUP }
            PinLockScreen(
                onNavigateBack = { navController.navigateUp() },
                mode = mode
            )
        }
    }
}
