package com.bowlingclub.app.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.bowlingclub.app.data.local.BowlingClubDatabase
import com.bowlingclub.app.data.local.entity.GameScore
import com.bowlingclub.app.data.local.entity.Member
import com.bowlingclub.app.data.local.entity.Tournament
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvExporter @Inject constructor(
    private val database: BowlingClubDatabase,
    @ApplicationContext private val context: Context
) {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /**
     * 모든 점수 데이터를 CSV로 내보내기
     * 형식: 대회명,대회일자,회원이름,게임번호,점수,핸디캡점수,최종점수,기록방식,입력일시
     */
    suspend fun exportAllScores(): Result<Uri> = try {
        val gameScores = database.gameScoreDao().getAllScores().firstOrNull() ?: emptyList()
        val tournaments = database.tournamentDao().getAllTournaments().firstOrNull()
            ?.associateBy { it.id } ?: emptyMap()
        val members = database.memberDao().getAllMembers().firstOrNull()
            ?.associateBy { it.id } ?: emptyMap()

        val fileName = "bowling_scores_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))}.csv"
        val file = createCsvFile(fileName)

        FileWriter(file, Charsets.UTF_8).use { writer ->
            // UTF-8 BOM 추가
            writer.write("\uFEFF")

            // 헤더
            writer.write("대회명,대회일자,회원이름,게임번호,점수,핸디캡점수,최종점수,기록방식,입력일시\n")

            // 데이터
            gameScores.forEach { score ->
                val tournament = tournaments[score.tournamentId]
                val member = members[score.memberId]

                val tournamentName = escapeCSV(tournament?.name ?: "")
                val tournamentDate = tournament?.date ?: ""
                val memberName = escapeCSV(member?.name ?: "")
                val gameNumber = score.gameNumber
                val baseScore = score.score
                val handicap = score.handicapScore
                val finalScore = score.finalScore
                val recordMethod = escapeCSV(score.recordedBy)
                val createdAt = score.createdAt

                val line = "$tournamentName,$tournamentDate,$memberName,$gameNumber,$baseScore,$handicap,$finalScore,$recordMethod,$createdAt\n"
                writer.write(line)
            }
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        Result.success(uri)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * 특정 대회의 점수 데이터를 CSV로 내보내기
     * 형식: 대회명,대회일자,회원이름,게임번호,점수,핸디캡점수,최종점수,기록방식,입력일시
     */
    suspend fun exportTournamentScores(tournamentId: Int): Result<Uri> = try {
        val gameScores = database.gameScoreDao().getScoresByTournament(tournamentId).firstOrNull() ?: emptyList()
        val tournament = database.tournamentDao().getTournamentById(tournamentId).firstOrNull()
        val members = database.memberDao().getAllMembers().firstOrNull()
            ?.associateBy { it.id } ?: emptyMap()

        val fileName = "bowling_scores_tournament_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.csv"
        val file = createCsvFile(fileName)

        FileWriter(file, Charsets.UTF_8).use { writer ->
            // UTF-8 BOM 추가
            writer.write("\uFEFF")

            // 헤더
            writer.write("대회명,대회일자,회원이름,게임번호,점수,핸디캡점수,최종점수,기록방식,입력일시\n")

            // 데이터
            gameScores.forEach { score ->
                val member = members[score.memberId]

                val tournamentName = escapeCSV(tournament?.name ?: "")
                val tournamentDate = tournament?.date ?: ""
                val memberName = escapeCSV(member?.name ?: "")
                val gameNumber = score.gameNumber
                val baseScore = score.score
                val handicap = score.handicapScore
                val finalScore = score.finalScore
                val recordMethod = escapeCSV(score.recordedBy)
                val createdAt = score.createdAt

                val line = "$tournamentName,$tournamentDate,$memberName,$gameNumber,$baseScore,$handicap,$finalScore,$recordMethod,$createdAt\n"
                writer.write(line)
            }
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        Result.success(uri)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * 회원 목록을 CSV로 내보내기
     * 형식: 이름,닉네임,성별,연락처,활동상태,가입일
     */
    suspend fun exportMembers(): Result<Uri> = try {
        val members = database.memberDao().getAllMembers().firstOrNull() ?: emptyList()

        val fileName = "bowling_members_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))}.csv"
        val file = createCsvFile(fileName)

        FileWriter(file, Charsets.UTF_8).use { writer ->
            // UTF-8 BOM 추가
            writer.write("\uFEFF")

            // 헤더
            writer.write("이름,닉네임,성별,연락처,활동상태,가입일\n")

            // 데이터
            members.forEach { member ->
                val name = escapeCSV(member.name)
                val nickname = escapeCSV(member.nickname ?: "")
                val gender = escapeCSV(member.gender ?: "")
                val phoneNumber = escapeCSV(member.phoneNumber ?: "")
                val isActive = if (member.isActive) "활동중" else "비활동"
                val joinDate = member.joinDate ?: ""

                val line = "$name,$nickname,$gender,$phoneNumber,$isActive,$joinDate\n"
                writer.write(line)
            }
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        Result.success(uri)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * CSV 특수문자 이스케이프 처리
     * 값에 쉼표가 있으면 큰따옴표로 감싸기
     */
    private fun escapeCSV(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    /**
     * CSV 파일 생성
     */
    private fun createCsvFile(fileName: String): File {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        return File(exportDir, fileName)
    }
}
