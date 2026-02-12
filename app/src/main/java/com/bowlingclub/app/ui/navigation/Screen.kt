package com.bowlingclub.app.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object MemberList : Screen("members")
    object MemberDetail : Screen("member/{memberId}") {
        fun createRoute(memberId: Int) = "member/$memberId"
    }
    object MemberEdit : Screen("member/edit?memberId={memberId}") {
        fun createRoute(memberId: Int? = null) =
            if (memberId != null) "member/edit?memberId=$memberId"
            else "member/edit"
    }
    object TournamentList : Screen("tournaments")
    object TournamentDetail : Screen("tournament/{tournamentId}") {
        fun createRoute(tournamentId: Int) = "tournament/$tournamentId"
    }
    object TournamentCreate : Screen("tournament/create")
    object ParticipantCheck : Screen("tournament/{tournamentId}/participants") {
        fun createRoute(tournamentId: Int) = "tournament/$tournamentId/participants"
    }
    object ScoreInput : Screen("tournament/{tournamentId}/scores") {
        fun createRoute(tournamentId: Int) = "tournament/$tournamentId/scores"
    }
    object TeamAssign : Screen("tournament/{tournamentId}/team-assign") {
        fun createRoute(tournamentId: Int) = "tournament/$tournamentId/team-assign"
    }
    object TeamResult : Screen("tournament/{tournamentId}/team-result") {
        fun createRoute(tournamentId: Int) = "tournament/$tournamentId/team-result"
    }
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
    object PinLock : Screen("pin-lock?mode={mode}") {
        fun createRoute(mode: String = "VERIFY") = "pin-lock?mode=$mode"
    }
}
