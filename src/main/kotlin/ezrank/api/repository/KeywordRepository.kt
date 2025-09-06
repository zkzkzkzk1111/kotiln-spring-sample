package ezrank.api.repository

import ezrank.api.entity.Keyword
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface KeywordRepository : JpaRepository<Keyword, Long> {
    
    @Modifying
    @Query("DELETE FROM keyword WHERE place_idx = :placeIdx AND keyword_name = :keywordName", nativeQuery = true)
    fun deleteByPlaceIdxAndKeywordName(@Param("placeIdx") placeIdx: Int, @Param("keywordName") keywordName: String): Int

    @Modifying
    @Query("DELETE FROM keyword WHERE keyword_idx = :keywordIdx", nativeQuery = true)
    fun deleteByKeywordIdx(@Param("keywordIdx") keywordIdx: Int): Int

    fun findKeyword(placeIdx: Int, keywordName: String): Keyword?

    @Modifying
    @Query("UPDATE Keyword k SET k.keywordDate = CURRENT_TIMESTAMP WHERE k.keywordIdx = :keywordIdx")
    fun updateKeywordDate(@Param("keywordIdx") keywordIdx: Int): Int
}