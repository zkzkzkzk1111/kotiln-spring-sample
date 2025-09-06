package ezrank.api.controller

import ezrank.api.dto.AuthDto
import ezrank.api.dto.RankDto
import ezrank.api.service.AuthService
import ezrank.api.service.RankService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kotlinx.coroutines.runBlocking

@Tag(name = "랭킹 API", description = "랭킹 관련 API")
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = ["*"])
class RankController(
    private val rankService: RankService,
    private val authService: AuthService
) {

    @Operation(summary = "랭킹 목록 조회", description = "랭킹 조회")
    @ApiResponse(responseCode = "200", description = "랭킹 목록 조회 성공",
        content = [Content(
            mediaType = "application/json",
            examples = [ExampleObject(value = """
            [
                {
                    "rank_idx": 1,
                    "search_query": "카페",
                    "place_id": 12345,
                    "rank_name": "스타벅스 강남점",
                    "rank_position": 3,
                    "created_at": "2024-01-15T10:30:00",
                    "keyword_idx": 123
                },
                {
                    "rank_idx": 2,
                    "search_query": "맛집",
                    "place_id": 67890,
                    "rank_name": "김밥천국 역삼점",
                    "rank_position": 1,
                    "created_at": "2024-01-14T15:20:00",
                    "keyword_idx": 456
                }
            ]
            """)]
        )])
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/ranks")
    fun getRanks(@Parameter(description = "Bearer 토큰") @RequestHeader("Authorization") token: String): ResponseEntity<Any> {
        val userIdx = authService.getCurrentUserIdx(token)
            ?: return ResponseEntity.badRequest().body(mapOf("message" to "유효하지 않은 토큰입니다."))

        val ranks = rankService.getRanks(userIdx)
        return ResponseEntity.ok(ranks)
    }

    @Operation(summary = "랭킹 결과 저장", description = "랭킹 결과를 저장")
    @PostMapping("/save-rank-result")
    fun saveRankResult(@RequestBody request: RankDto.SaveRankRequest): ResponseEntity<Any> {
        return rankService.createRank(request).fold(
            onSuccess = { response ->
                ResponseEntity.ok(response)
            },
            onFailure = { error ->
                ResponseEntity.status(500).body(AuthDto.ErrorResponse(error.message ?: "저장 실패"))
            }
        )
    }

    @Operation(summary = "다중 랭킹 결과 저장", description = "여러 랭킹 결과를 한번에 저장")
    @ApiResponse(responseCode = "200", description = "다중 랭킹 저장 성공",
        content = [Content(
            mediaType = "application/json",
            examples = [ExampleObject(value = """
            {
                "message": "총 2개의 랭킹 데이터가 성공적으로 저장되었습니다."
            }
            """)]
        )])
    @PostMapping("/save-rank-results")
    fun saveRankResults(@RequestBody request: List<RankDto.SaveRankRequest>): ResponseEntity<Any> {
        println("🔍 [save-rank-results] 엔드포인트 호출됨")
        println("🔍 [save-rank-results] 요청 데이터 크기: ${request.size}")
        println("🔍 [save-rank-results] 요청 데이터: $request")
        
        return rankService.createRanks(request).fold(
            onSuccess = { response ->
                println("🔍 [save-rank-results] 저장 성공: $response")
                ResponseEntity.ok(response)
            },
            onFailure = { error ->
                println("🔍 [save-rank-results] 저장 실패: ${error.message}")
                error.printStackTrace()
                ResponseEntity.status(500).body(AuthDto.ErrorResponse(error.message ?: "저장 실패"))
            }
        )
    }

    @Operation(summary = "통계 데이터 조회", description = "랭킹 통계 데이터 조회")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/stats")
    fun getStats(@Parameter(description = "Bearer 토큰") @RequestHeader("Authorization") token: String): ResponseEntity<Any> {
        authService.getCurrentUserIdx(token)
            ?: return ResponseEntity.badRequest().body(mapOf("message" to "유효하지 않은 토큰입니다."))

        val stats = rankService.getStatistics()
        return ResponseEntity.ok(stats)
    }

    @Operation(summary = "랭킹 삭제", description = "랭킹 데이터 삭제")
    @PostMapping("/rank-delete")
    fun deleteRank(@RequestBody request: RankDto.DeleteRankRequest): ResponseEntity<Any> {
        return rankService.deleteRank(request).fold(
            onSuccess = { response ->
                ResponseEntity.ok(response)
            },
            onFailure = { _ ->
                ResponseEntity.status(500).body(mapOf("message" to "삭제 실패 했습니다."))
            }
        )
    }

    @Operation(summary = "스케줄링 - 모든 랭킹 데이터 배치 처리", description = "rank 테이블의 모든 데이터를 20개씩 나누어 크롤링 서버로 전송 (수동 실행용)")
    @PostMapping("/schedule-rank-processing")
    fun scheduleRankProcessing(): ResponseEntity<Any> {
        return runBlocking {
            rankService.processAllRankDataInBatches().fold(
                onSuccess = { message ->
                    ResponseEntity.ok(mapOf("message" to message))
                },
                onFailure = { error ->
                    ResponseEntity.status(500).body(mapOf("message" to "스케줄링 처리 실패: ${error.message}"))
                }
            )
        }
    }
}