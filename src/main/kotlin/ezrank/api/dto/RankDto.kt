package ezrank.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

object RankDto {

    @Schema(description = "랭킹 조회 응답 DTO", example = """
    {
        "rank_idx": 1,
        "search_query": "카페",
        "place_id": 12345,
        "rank_name": "스타벅스 강남점",
        "rank_position": 3,
        "created_at": "2024-01-15T10:30:00",
        "keyword_idx": 123
    }
    """)
    data class RankResponse(
        @Schema(description = "랭킹 인덱스")
        val rank_idx: Int,
        @Schema(description = "검색 쿼리")
        val search_query: String,
        @Schema(description = "장소 ID")
        val place_id: Int,
        @Schema(description = "랭킹 이름")
        val rank_name: String,
        @Schema(description = "랭킹 순위")
        val rank_position: Int,
        @Schema(description = "생성 날짜")
        val created_at: LocalDateTime,
        @Schema(description = "키워드 인덱스")
        val keyword_idx: Int
    )

    @Schema(description = "랭킹 저장 요청 DTO", example = """
    {
        "search_query": "카페",
        "place_id": "place123",
        "rank_position": 3,
        "place_name": "스타벅스 강남점",
        "userIdx": 1
    }
    """)
    data class SaveRankRequest(
        @Schema(description = "검색 쿼리", example = "카페")
        val search_query: String,
        @Schema(description = "장소 ID", example = "place123")
        val place_id: Any,  // String 또는 Int 모두 받을 수 있도록 변경
        @Schema(description = "랭킹 순위", example = "1")
        val rank_position: Int,
        @Schema(description = "장소 이름", example = "스타벅스")
        val place_name: String? = null,
        @Schema(description = "사용자 인덱스", example = "1")
        val userIdx: Int,
        @Schema(description = "트래픽 정보", example = "0.0")
        val traffic: Double? = null  // 선택적 필드로 추가
    )

    @Schema(description = "저장 성공 응답 DTO", example = """
    {
        "message": "랭킹 데이터가 성공적으로 저장되었습니다."
    }
    """)
    data class SaveResponse(
        @Schema(description = "응답 메시지")
        val message: String
    )

    @Schema(description = "통계 데이터 응답 DTO", example = """
    {
        "total_keywords": 25,
        "avg_rank": 3.4,
        "top3_count": 8,
        "today_count": 5
    }
    """)
    data class StatsResponse(
        @Schema(description = "총 키워드 갯수")
        val total_keywords: Int,
        @Schema(description = "평균 랭킹")
        val avg_rank: Double,
        @Schema(description = "Top3 갯수")
        val top3_count: Int,
        @Schema(description = "오늘 검색 갯수")
        val today_count: Int
    )

    @Schema(description = "다중 랭킹 저장 요청 DTO", example = """
    [
        {
            "search_query": "123",
            "place_id": "123",
            "rank_position": 1,
            "place_name": "장소명1",
            "userIdx": 123
        },
        {
            "search_query": "456",
            "place_id": "456",
            "rank_position": 2,
            "place_name": "장소명2",
            "userIdx": 456
        }
    ]
    """)
    data class SaveRankRequestList(
        @Schema(description = "랭킹 저장 요청 목록")
        val ranks: List<SaveRankRequest>
    )

    @Schema(description = "랭킹 삭제 요청 DTO")
    data class DeleteRankRequest(
        @Schema(description = "랭킹 인덱스", example = "1")
        val rankIdx: Int,
        @Schema(description = "사용자 인덱스", example = "1")
        val userIdx: Int
    )
}