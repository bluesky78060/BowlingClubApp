package com.bowlingclub.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.room.withTransaction
import com.bowlingclub.app.data.local.BowlingClubDatabase
import com.bowlingclub.app.data.local.entity.*
import com.google.gson.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import java.io.File
import java.io.InputStream
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    private val database: BowlingClubDatabase,
    @ApplicationContext private val context: Context
) {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .setPrettyPrinting()
        .create()

    data class BackupData(
        val version: Int = 1,
        val createdAt: String,
        val appVersion: String,
        val members: List<Member>,
        val tournaments: List<Tournament>,
        val participants: List<TournamentParticipant>,
        val scores: List<GameScore>,
        val teams: List<Team>,
        val teamMembers: List<TeamMember>,
        val settings: List<Setting>
    )

    data class RestoreResult(
        val membersRestored: Int,
        val tournamentsRestored: Int,
        val scoresRestored: Int,
        val teamsRestored: Int
    )

    suspend fun createBackup(): Result<Uri> {
        return try {
            val memberDao = database.memberDao()
            val tournamentDao = database.tournamentDao()
            val participantDao = database.tournamentParticipantDao()
            val scoreDao = database.gameScoreDao()
            val teamDao = database.teamDao()
            val teamMemberDao = database.teamMemberDao()
            val settingDao = database.settingDao()

            val members = memberDao.getAllMembers().firstOrNull() ?: emptyList()
            val tournaments = tournamentDao.getAllTournaments().firstOrNull() ?: emptyList()
            val participants = participantDao.getAllParticipants().firstOrNull() ?: emptyList()
            val scores = scoreDao.getAllScores().firstOrNull() ?: emptyList()
            val teams = teamDao.getAllTeams().firstOrNull() ?: emptyList()
            val teamMembers = teamMemberDao.getAllTeamMembers().firstOrNull() ?: emptyList()
            val settings = settingDao.getAllSettings().firstOrNull() ?: emptyList()

            val backupData = BackupData(
                version = 1,
                createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                appVersion = getAppVersion(),
                members = members,
                tournaments = tournaments,
                participants = participants,
                scores = scores,
                teams = teams,
                teamMembers = teamMembers,
                settings = settings
            )

            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val fileName = "bowling_backup_$timestamp.json"
            val backupDir = File(context.cacheDir, "backups").apply { mkdirs() }
            val backupFile = File(backupDir, fileName)

            backupFile.writeText(gson.toJson(backupData))

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )

            settingDao.insertOrUpdate(
                Setting(
                    key = "last_backup_time",
                    value = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )
            )

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreFromUri(uri: Uri): Result<RestoreResult> {
        return try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(IllegalArgumentException("백업 파일을 열 수 없습니다"))

            val jsonString = inputStream.bufferedReader().use { it.readText() }

            if (jsonString.isBlank()) {
                return Result.failure(IllegalArgumentException("백업 파일이 비어있습니다"))
            }

            val backupData = try {
                gson.fromJson(jsonString, BackupData::class.java)
            } catch (e: JsonSyntaxException) {
                return Result.failure(IllegalArgumentException("백업 파일 형식이 올바르지 않습니다: ${e.message}"))
            } catch (e: JsonParseException) {
                return Result.failure(IllegalArgumentException("백업 파일을 읽을 수 없습니다: ${e.message}"))
            }

            if (backupData == null) {
                return Result.failure(IllegalArgumentException("백업 데이터가 유효하지 않습니다"))
            }

            if (backupData.version != 1) {
                return Result.failure(IllegalArgumentException("지원하지 않는 백업 버전입니다: ${backupData.version}"))
            }

            database.withTransaction {
                database.teamMemberDao().deleteAll()
                database.teamDao().deleteAll()
                database.gameScoreDao().deleteAll()
                database.tournamentParticipantDao().deleteAll()
                database.tournamentDao().deleteAll()
                database.memberDao().deleteAll()
                database.settingDao().deleteAll()

                backupData.members?.forEach { member ->
                    database.memberDao().insert(member)
                }

                backupData.tournaments?.forEach { tournament ->
                    database.tournamentDao().insert(tournament)
                }

                backupData.participants?.forEach { participant ->
                    database.tournamentParticipantDao().insert(participant)
                }

                backupData.scores?.forEach { score ->
                    database.gameScoreDao().insert(score)
                }

                backupData.teams?.forEach { team ->
                    database.teamDao().insert(team)
                }

                backupData.teamMembers?.forEach { teamMember ->
                    database.teamMemberDao().insert(teamMember)
                }

                backupData.settings?.forEach { setting ->
                    database.settingDao().insertOrUpdate(setting)
                }
            }

            val result = RestoreResult(
                membersRestored = backupData.members?.size ?: 0,
                tournamentsRestored = backupData.tournaments?.size ?: 0,
                scoresRestored = backupData.scores?.size ?: 0,
                teamsRestored = backupData.teams?.size ?: 0
            )

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun shareBackup(): Result<Uri> {
        return try {
            val backupResult = createBackup()
            if (backupResult.isFailure) {
                return Result.failure(backupResult.exceptionOrNull() ?: Exception("Backup creation failed"))
            }

            val uri = backupResult.getOrThrow()

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(shareIntent, "백업 파일 공유").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLastBackupTime(): LocalDateTime? {
        return try {
            val value = database.settingDao().getSettingValue("last_backup_time")
            value?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }
        } catch (e: Exception) {
            null
        }
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private class LocalDateAdapter : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
        override fun serialize(src: LocalDate?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            return JsonPrimitive(src?.format(DateTimeFormatter.ISO_LOCAL_DATE))
        }

        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalDate? {
            val dateString = json?.asString
            return if (dateString.isNullOrBlank()) {
                null
            } else {
                try {
                    LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    private class LocalDateTimeAdapter : JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        override fun serialize(src: LocalDateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            return JsonPrimitive(src?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        }

        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalDateTime? {
            val dateTimeString = json?.asString
            return if (dateTimeString.isNullOrBlank()) {
                null
            } else {
                try {
                    LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
}
