package com.bowlingclub.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bowlingclub.app.data.local.BowlingClubDatabase
import com.bowlingclub.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE members ADD COLUMN birthDate TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE members ADD COLUMN address TEXT DEFAULT NULL")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BowlingClubDatabase {
        return Room.databaseBuilder(
            context,
            BowlingClubDatabase::class.java,
            "bowling_club_db"
        )
            .addMigrations(MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideMemberDao(db: BowlingClubDatabase): MemberDao = db.memberDao()

    @Provides
    fun provideTournamentDao(db: BowlingClubDatabase): TournamentDao = db.tournamentDao()

    @Provides
    fun provideTournamentParticipantDao(db: BowlingClubDatabase): TournamentParticipantDao =
        db.tournamentParticipantDao()

    @Provides
    fun provideGameScoreDao(db: BowlingClubDatabase): GameScoreDao = db.gameScoreDao()

    @Provides
    fun provideTeamDao(db: BowlingClubDatabase): TeamDao = db.teamDao()

    @Provides
    fun provideTeamMemberDao(db: BowlingClubDatabase): TeamMemberDao = db.teamMemberDao()

    @Provides
    fun provideSettingDao(db: BowlingClubDatabase): SettingDao = db.settingDao()
}
