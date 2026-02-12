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
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import java.time.LocalDate
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
     * CSV 파일에서 회원 데이터 가져오기
     * 형식: 이름,닉네임,성별,연락처,활동상태,가입일
     */
    suspend fun importMembers(uri: Uri): Result<Int> = try {
        val members = mutableListOf<Member>()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                var firstLine = true
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line!!.trim()
                        .removePrefix("\uFEFF") // BOM 제거

                    if (currentLine.isBlank()) continue

                    // 첫 줄은 헤더 → 스킵
                    if (firstLine) {
                        firstLine = false
                        continue
                    }

                    val fields = parseCsvLine(currentLine)
                    if (fields.size < 1) continue

                    val name = fields[0].trim()
                    if (name.isBlank()) continue

                    val nickname = fields.getOrNull(1)?.trim()?.ifBlank { null }
                    val genderRaw = fields.getOrNull(2)?.trim() ?: ""
                    val gender = if (genderRaw == "여" || genderRaw == "F") "F" else "M"
                    val phoneNumber = fields.getOrNull(3)?.trim()?.ifBlank { null }
                    val activeRaw = fields.getOrNull(4)?.trim() ?: "활동중"
                    val isActive = activeRaw != "비활동"
                    val joinDateStr = fields.getOrNull(5)?.trim() ?: ""
                    val joinDate = try {
                        LocalDate.parse(joinDateStr)
                    } catch (e: Exception) {
                        LocalDate.now()
                    }

                    members.add(
                        Member(
                            name = name,
                            nickname = nickname,
                            gender = gender,
                            phoneNumber = phoneNumber,
                            isActive = isActive,
                            joinDate = joinDate,
                            createdAt = LocalDateTime.now(),
                            updatedAt = LocalDateTime.now()
                        )
                    )
                }
            }
        } ?: throw Exception("파일을 열 수 없습니다")

        if (members.isEmpty()) throw Exception("가져올 회원 데이터가 없습니다")

        database.memberDao().insertAll(members)
        Result.success(members.size)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * CSV 한 줄을 파싱 (따옴표 처리 포함)
     */
    private fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    fields.add(current.toString())
                    current.clear()
                }
                else -> current.append(char)
            }
        }
        fields.add(current.toString())
        return fields
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
