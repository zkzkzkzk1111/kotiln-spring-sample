package ezrank.api.service

import ezrank.api.dto.RankDto
import ezrank.api.repository.RankRepository
import ezrank.api.repository.PlaceRepository
import ezrank.api.repository.KeywordRepository
import ezrank.api.entity.Place
import ezrank.api.entity.Keyword
import ezrank.api.entity.Rank
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlinx.coroutines.*
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URI
import com.fasterxml.jackson.databind.ObjectMapper

@Service
@Transactional
class RankService(
    private val rankRepository: RankRepository,
    private val placeRepository: PlaceRepository,
    private val keywordRepository: KeywordRepository
) {
    @Transactional(readOnly = true)
    fun getRanks(userIdx: Int): List<RankDto.RankResponse> {
        return try {
            val results = rankRepository.findRanksByUserIdx(userIdx.toLong())

            results.map { result ->
                RankDto.RankResponse(
                    rank_idx = result[5] as Int,
                    search_query = result[0] as String,
                    place_id = result[1] as Int,
                    rank_name = result[4] as String,
                    rank_position = result[2] as Int,
                    created_at = (result[3] as java.sql.Timestamp).toLocalDateTime(),
                    keyword_idx = result[6] as Int
                )
            }
        } catch (e: Exception) {
            println("DB 조회 중 오류: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    fun createRank(request: RankDto.SaveRankRequest): Result<RankDto.SaveResponse> {
        return try {
            val errors = validateRankInput(request)
            if (errors.isNotEmpty()) {
                return Result.failure(Exception(errors.joinToString(", ")))
            }

            val placeIdInt = when (request.place_id) {
                is String -> request.place_id.toInt()
                is Number -> request.place_id.toInt()
                else -> throw IllegalArgumentException("place_id는 문자열 또는 숫자여야 합니다.")
            }

            // 중복 체크: rank_num, user_idx, rank_name, keyword_name, place_id, 년월일이 같은 데이터가 있는지 확인
            val existingRankIdx = rankRepository.findExistingRank(
                rankNum = request.rank_position,
                userIdx = request.userIdx,
                rankName = request.place_name ?: "",
                keywordName = request.search_query,
                placeId = placeIdInt
            )

            if (existingRankIdx != null) {
                // 기존 데이터가 있으면 UPDATE (rank_date만 업데이트)
                rankRepository.updateRankDate(existingRankIdx)
                Result.success(RankDto.SaveResponse(message = "기존 데이터 업데이트 완료"))
            } else {
                // 기존 데이터가 없으면 새로 INSERT
                
                // Place 중복 체크 및 처리
                val existingPlace = placeRepository.findFirstByUserIdxAndPlaceIdOrderByPlaceIdx(request.userIdx, placeIdInt)
                val finalPlace = if (existingPlace != null) {
                    // 기존 Place가 있으면 날짜 업데이트
                    placeRepository.updatePlaceDate(existingPlace.placeIdx!!)
                    existingPlace
                } else {
                    // 새로운 Place 생성
                    val place = Place(
                        userIdx = request.userIdx,
                        placeId = placeIdInt
                    )
                    placeRepository.save(place)
                }

                // Keyword 중복 체크 및 처리
                val existingKeyword = keywordRepository.findKeyword(
                    finalPlace.placeIdx!!, 
                    request.search_query
                )
                val finalKeyword = if (existingKeyword != null) {
                    // 기존 Keyword가 있으면 날짜 업데이트
                    keywordRepository.updateKeywordDate(existingKeyword.keywordIdx!!)
                    existingKeyword
                } else {
                    // 새로운 Keyword 생성
                    val keyword = Keyword(
                        placeIdx = finalPlace.placeIdx!!,
                        keywordName = request.search_query
                    )
                    keywordRepository.save(keyword)
                }

                val rank = Rank(
                    keywordIdx = finalKeyword.keywordIdx!!,
                    rankName = request.place_name ?: "",
                    rankNum = request.rank_position,
                    userIdx = request.userIdx
                )
                rankRepository.save(rank)
                Result.success(RankDto.SaveResponse(message = "신규 데이터 저장 완료"))
            }

        } catch (e: Exception) {
            println("저장 중 오류: ${e.message}")
            Result.failure(e)
        }
    }

    fun createRanks(requests: List<RankDto.SaveRankRequest>): Result<RankDto.SaveResponse> {
        return try {
            println("🔍 [RankService.createRanks] 시작 - 요청 개수: ${requests.size}")
            
            if (requests.isEmpty()) {
                println("🔍 [RankService.createRanks] 빈 요청 리스트")
                return Result.failure(Exception("저장할 데이터가 없습니다."))
            }

            var successCount = 0
            var updateCount = 0
            val errors = mutableListOf<String>()

            requests.forEachIndexed { index, request ->
                try {
                    println("🔍 [RankService.createRanks] 항목 ${index + 1} 처리 시작: $request")
                    
                    val validationErrors = validateRankInput(request)
                    if (validationErrors.isNotEmpty()) {
                        println("🔍 [RankService.createRanks] 항목 ${index + 1} 검증 실패: $validationErrors")
                        errors.add("항목 ${index + 1}: ${validationErrors.joinToString(", ")}")
                        return@forEachIndexed
                    }

                    val placeIdInt = when (request.place_id) {
                        is String -> request.place_id.toInt()
                        is Number -> request.place_id.toInt()
                        else -> throw IllegalArgumentException("place_id는 문자열 또는 숫자여야 합니다.")
                    }
                    println("🔍 [RankService.createRanks] 항목 ${index + 1} placeId 변환: $placeIdInt (원본: ${request.place_id})")

                    // 중복 체크: rank_num, user_idx, rank_name, keyword_name, place_id, 년월일이 같은 데이터가 있는지 확인
                    val existingRankIdx = rankRepository.findExistingRank(
                        rankNum = request.rank_position,
                        userIdx = request.userIdx,
                        rankName = request.place_name ?: "",
                        keywordName = request.search_query,
                        placeId = placeIdInt
                    )

                    if (existingRankIdx != null) {
                        // 기존 데이터가 있으면 UPDATE (rank_date만 업데이트)
                        rankRepository.updateRankDate(existingRankIdx)
                        println("🔍 [RankService.createRanks] 항목 ${index + 1} 기존 데이터 업데이트 완료")
                        updateCount++
                    } else {
                        // 기존 데이터가 없으면 새로 INSERT
                        
                        // Place 중복 체크 및 처리
                        val existingPlace = placeRepository.findFirstByUserIdxAndPlaceIdOrderByPlaceIdx(request.userIdx, placeIdInt)
                        val finalPlace = if (existingPlace != null) {
                            // 기존 Place가 있으면 날짜 업데이트
                            placeRepository.updatePlaceDate(existingPlace.placeIdx!!)
                            println("🔍 [RankService.createRanks] 항목 ${index + 1} 기존 Place 업데이트: placeIdx=${existingPlace.placeIdx}")
                            existingPlace
                        } else {
                            // 새로운 Place 생성
                            val place = Place(
                                userIdx = request.userIdx,
                                placeId = placeIdInt
                            )
                            val savedPlace = placeRepository.save(place)
                            println("🔍 [RankService.createRanks] 항목 ${index + 1} 새 Place 저장 완료: placeIdx=${savedPlace.placeIdx}")
                            savedPlace
                        }

                        // Keyword 중복 체크 및 처리
                        val existingKeyword = keywordRepository.findKeyword(
                            finalPlace.placeIdx!!, 
                            request.search_query
                        )
                        val finalKeyword = if (existingKeyword != null) {
                            // 기존 Keyword가 있으면 날짜 업데이트
                            keywordRepository.updateKeywordDate(existingKeyword.keywordIdx!!)
                            println("🔍 [RankService.createRanks] 항목 ${index + 1} 기존 Keyword 업데이트: keywordIdx=${existingKeyword.keywordIdx}")
                            existingKeyword
                        } else {
                            // 새로운 Keyword 생성
                            val keyword = Keyword(
                                placeIdx = finalPlace.placeIdx!!,
                                keywordName = request.search_query
                            )
                            val savedKeyword = keywordRepository.save(keyword)
                            println("🔍 [RankService.createRanks] 항목 ${index + 1} 새 Keyword 저장 완료: keywordIdx=${savedKeyword.keywordIdx}")
                            savedKeyword
                        }

                        val rank = Rank(
                            keywordIdx = finalKeyword.keywordIdx!!,
                            rankName = request.place_name ?: "",
                            rankNum = request.rank_position,
                            userIdx = request.userIdx
                        )
                        rankRepository.save(rank)
                        println("🔍 [RankService.createRanks] 항목 ${index + 1} 신규 데이터 저장 완료")
                        successCount++
                    }

                } catch (e: Exception) {
                    println("🔍 [RankService.createRanks] 항목 ${index + 1} 저장 오류: ${e.message}")
                    e.printStackTrace()
                    errors.add("항목 ${index + 1} 저장 오류: ${e.message}")
                }
            }

            if (errors.isNotEmpty()) {
                println("🔍 [RankService.createRanks] 오류 발생: $errors")
                return Result.failure(Exception("저장 중 오류 발생: ${errors.joinToString("; ")}"))
            }

            println("🔍 [RankService.createRanks] 성공 완료 - 신규 저장: $successCount, 업데이트: $updateCount")
            val totalCount = successCount + updateCount
            val message = when {
                successCount > 0 && updateCount > 0 -> "총 ${totalCount}개의 랭킹 데이터가 처리되었습니다. (신규 저장: ${successCount}개, 업데이트: ${updateCount}개)"
                successCount > 0 -> "총 ${successCount}개의 랭킹 데이터가 성공적으로 저장되었습니다."
                updateCount > 0 -> "총 ${updateCount}개의 랭킹 데이터가 업데이트되었습니다."
                else -> "처리된 데이터가 없습니다."
            }
            Result.success(RankDto.SaveResponse(message = message))

        } catch (e: Exception) {
            println("🔍 [RankService.createRanks] 예외 발생: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    @Transactional(readOnly = true)
    fun getStatistics(): RankDto.StatsResponse {
        return try {
            val totalKeywords = rankRepository.countTotalKeywords()
            val avgRank = rankRepository.findAverageRank() ?: 0.0
            val top3Count = rankRepository.countTop3Ranks()
            val todayCount = rankRepository.countTodayRanks()

            RankDto.StatsResponse(
                total_keywords = totalKeywords,
                avg_rank = Math.round(avgRank * 10.0) / 10.0,
                top3_count = top3Count,
                today_count = todayCount
            )
        } catch (e: Exception) {
            println("통계 조회 중 오류: ${e.message}")
            RankDto.StatsResponse(
                total_keywords = 0,
                avg_rank = 0.0,
                top3_count = 0,
                today_count = 0
            )
        }
    }

    fun deleteRank(request: RankDto.DeleteRankRequest): Result<Map<String, String>> {
        return try {
            // rank 테이블에서만 삭제 (keyword와 place는 다른 사용자가 사용할 수 있으므로 삭제하지 않음)
            val rankDeleted = rankRepository.deleteByRankIdxAndUserIdx(request.rankIdx, request.userIdx)
            
            if (rankDeleted > 0) {
                Result.success(mapOf("message" to "랭킹 데이터 삭제 완료"))
            } else {
                Result.failure(Exception("삭제할 랭킹 데이터가 없습니다."))
            }
        } catch (e: Exception) {
            println("삭제 중 오류: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun validateRankInput(rankData: RankDto.SaveRankRequest): List<String> {
        val errors = mutableListOf<String>()

        if (rankData.search_query.isBlank()) {
            errors.add("search_query는 필수입니다.")
        }

        when (rankData.place_id) {
            is String -> if (rankData.place_id.isBlank()) errors.add("place_id는 필수입니다.")
            is Number -> if (rankData.place_id.toInt() == 0) errors.add("place_id는 필수입니다.")
            null -> errors.add("place_id는 필수입니다.")
            else -> errors.add("place_id는 문자열 또는 숫자여야 합니다.")
        }

        try {
            rankData.rank_position
        } catch (e: Exception) {
            errors.add("rank_position은 숫자여야 합니다.")
        }

        return errors
    }

    suspend fun processAllRankDataInBatches(): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val totalCount = rankRepository.countAllRanks()
            val batchSize = 10
            var offset = 0
            var totalProcessed = 0
            var totalSuccessful = 0

            println("🔍 [processAllRankDataInBatches] 시작 - 전체 데이터 개수: $totalCount")

            while (offset < totalCount) {
                val batchData = rankRepository.findAllRankDataForScheduling(batchSize, offset)
                
                if (batchData.isEmpty()) {
                    println("🔍 [processAllRankDataInBatches] 더 이상 처리할 데이터 없음")
                    break
                }

                val requestData = batchData.map { result ->
                    mapOf(
                        "userIdx" to (result[0] as Int),
                        "search_query" to (result[1] as String),
                        "place_id" to (result[2] as Int),
                        "place_name" to (result[3] as String)
                    )
                }

                println("🔍 [processAllRankDataInBatches] 배치 ${offset/batchSize + 1} 처리 중 - 데이터 개수: ${requestData.size}")

                val success = sendBatchToCrawlingServer(requestData)
                
                totalProcessed += requestData.size
                if (success) {
                    totalSuccessful += requestData.size
                }

                offset += batchSize

                // 배치 간 간격 (크롤링 서버 부하 방지)
                delay(5000) // 5초 대기
            }

            val message = "전체 $totalProcessed 개 데이터 처리 완료. 성공: $totalSuccessful 개"
            println("🔍 [processAllRankDataInBatches] 완료 - $message")
            Result.success(message)

        } catch (e: Exception) {
            println("🔍 [processAllRankDataInBatches] 오류: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private suspend fun sendBatchToCrawlingServer(batchData: List<Map<String, Any>>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val objectMapper = ObjectMapper()
                val jsonBody = objectMapper.writeValueAsString(batchData)

                val client = HttpClient.newHttpClient()
                val request = HttpRequest.newBuilder()
                    .uri(URI.create("http://52.78.81.141:5000/api/rank/bulk"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build()

                val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                val isSuccess = response.statusCode() in 200..299

                println("🔍 [sendBatchToCrawlingServer] 배치 전송 결과: ${response.statusCode()}, 성공: $isSuccess")
                isSuccess

            } catch (e: Exception) {
                println("🔍 [sendBatchToCrawlingServer] 배치 전송 오류: ${e.message}")
                false
            }
        }
    }
}