package com.bowlingclub.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bowlingclub.app.data.local.converter.DateConverters
import com.bowlingclub.app.data.local.dao.*
import com.bowlingclub.app.data.local.entity.*

@Database(
    entities = [
        Member::class,
        Tournament::class,
        TournamentParticipant::class,
        GameScore::class,
        Team::class,
        TeamMember::class,
        Setting::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(DateConverters::class)
abstract class BowlingClubDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun tournamentDao(): TournamentDao
    abstract fun tournamentParticipantDao(): TournamentParticipantDao
    abstract fun gameScoreDao(): GameScoreDao
    abstract fun teamDao(): TeamDao
    abstract fun teamMemberDao(): TeamMemberDao
    abstract fun settingDao(): SettingDao
}
