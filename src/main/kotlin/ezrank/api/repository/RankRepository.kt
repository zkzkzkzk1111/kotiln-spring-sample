package ezrank.api.repository

import ezrank.api.entity.Rank
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RankRepository : JpaRepository<Rank, Long> {

    @Query(value = """
        SELECT 
            k.keyword_name as search_query,
            p.place_id,
            r.rank_num as rank_position,
            r.rank_date as created_at,
            r.rank_name,
            r.rank_idx,
            r.keyword_idx
        FROM `rank` r
        JOIN keyword k ON r.keyword_idx = k.keyword_idx
        JOIN place p ON k.place_idx = p.place_idx
        WHERE r.rank_num != -1 AND r.user_idx = :userIdx
        ORDER BY r.rank_name DESC
        """, nativeQuery = true)
    fun findRanksByUserIdx(@Param("userIdx") userIdx: Long): List<Array<Any>>

    @Query("SELECT COUNT(DISTINCT k.keywordName) FROM Keyword k")
    fun countTotalKeywords(): Int

    @Query("SELECT AVG(r.rankNum) FROM Rank r WHERE r.rankNum != -1")
    fun findAverageRank(): Double?

    @Query("SELECT COUNT(r) FROM Rank r WHERE r.rankNum <= 3 AND r.rankNum != -1")
    fun countTop3Ranks(): Int

    @Query("SELECT COUNT(r) FROM Rank r WHERE DATE(r.rankDate) = CURRENT_DATE")
    fun countTodayRanks(): Int

    @Modifying
    @Query("DELETE FROM `rank` WHERE rank_idx = :rankIdx AND user_idx = :userIdx", nativeQuery = true)
    fun deleteByRankIdxAndUserIdx(@Param("rankIdx") rankIdx: Int, @Param("userIdx") userIdx: Int): Int

    @Modifying
    @Query(value = """
        DELETE FROM `rank` 
        WHERE rank_idx = :rankIdx 
        AND user_idx = :userIdx 
        AND keyword_idx IN (
            SELECT keyword_idx FROM keyword 
            WHERE keyword_name = :keywordName
        )
        """, nativeQuery = true)
    fun deleteByRankIdxUserIdxAndKeywordName(@Param("rankIdx") rankIdx: Int, @Param("userIdx") userIdx: Int, @Param("keywordName") keywordName: String): Int

    @Query(value = """
        SELECT k.place_idx, k.keyword_idx, p.place_id, k.keyword_name
        FROM `rank` r
        JOIN keyword k ON r.keyword_idx = k.keyword_idx
        JOIN place p ON k.place_idx = p.place_idx
        WHERE r.rank_idx = :rankIdx 
        AND r.user_idx = :userIdx
        """, nativeQuery = true)
    fun findPlaceAndKeywordInfo(@Param("rankIdx") rankIdx: Int, @Param("userIdx") userIdx: Int): List<Array<Any>>

    @Modifying
    @Query("DELETE FROM `rank` WHERE keyword_idx = :keywordIdx", nativeQuery = true)
    fun deleteByKeywordIdx(@Param("keywordIdx") keywordIdx: Int): Int

    @Query(value = """
        SELECT r.rank_idx FROM `rank` r
        JOIN keyword k ON r.keyword_idx = k.keyword_idx
        JOIN place p ON k.place_idx = p.place_idx
        WHERE r.rank_num = :rankNum 
        AND r.user_idx = :userIdx 
        AND r.rank_name = :rankName 
        AND k.keyword_name = :keywordName
        AND p.place_id = :placeId
        AND DATE(r.rank_date) = CURDATE()
        ORDER BY r.rank_idx ASC
        LIMIT 1
        """, nativeQuery = true)
    fun findExistingRank(
        @Param("rankNum") rankNum: Int,
        @Param("userIdx") userIdx: Int,
        @Param("rankName") rankName: String,
        @Param("keywordName") keywordName: String,
        @Param("placeId") placeId: Int
    ): Int?

    @Modifying
    @Query(value = """
        UPDATE `rank` 
        SET rank_date = CONVERT_TZ(NOW(), @@session.time_zone, '+09:00')
        WHERE rank_idx = :rankIdx
        """, nativeQuery = true)
    fun updateRankDate(@Param("rankIdx") rankIdx: Int): Int

    @Query(value = """
        SELECT 
            r.user_idx,
            k.keyword_name as search_query,
            p.place_id,
            r.rank_name as place_name
        FROM `rank` r
        JOIN keyword k ON r.keyword_idx = k.keyword_idx
        JOIN place p ON k.place_idx = p.place_idx
        GROUP BY r.user_idx, r.rank_name, r.keyword_idx
        ORDER BY MAX(r.rank_idx)
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    fun findAllRankDataForScheduling(@Param("limit") limit: Int, @Param("offset") offset: Int): List<Array<Any>>

    @Query(value = """
        SELECT COUNT(DISTINCT r.user_idx, r.rank_name) 
        FROM `rank` r
        """, nativeQuery = true)
    fun countAllRanks(): Long
}