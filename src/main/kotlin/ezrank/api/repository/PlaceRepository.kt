package ezrank.api.repository

import ezrank.api.entity.Place
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PlaceRepository : JpaRepository<Place, Long> {
    @Modifying
    @Query("DELETE FROM place WHERE user_idx = :userIdx AND place_id = :placeId", nativeQuery = true)
    fun deleteByUserIdxAndPlaceId(@Param("userIdx") userIdx: Int, @Param("placeId") placeId: Int): Int

    fun findFirstByUserIdxAndPlaceIdOrderByPlaceIdx(userIdx: Int, placeId: Int): Place?

    @Modifying
    @Query("UPDATE Place p SET p.placeDate = CURRENT_TIMESTAMP WHERE p.placeIdx = :placeIdx")
    fun updatePlaceDate(@Param("placeIdx") placeIdx: Int): Int
}