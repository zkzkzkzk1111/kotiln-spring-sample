package ezrank.api.repository

import ezrank.api.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {

    fun findByUserId(userId: String): User?

    fun findByUserIdx(userIdx: Long): User?

    fun findByUserEmail(userEmail: String): User?

    fun existsByUserId(userId: String): Boolean

    fun existsByUserEmail(userEmail: String): Boolean

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.userId = :userId OR u.userEmail = :email")
    fun existsByUserIdOrUserEmail(@Param("userId") userId: String, @Param("email") email: String): Boolean

    fun findByUserIdAndUserIdx(userId: String, userIdx: Long): User?

    fun findByUserNameAndUserEmail(userName: String, userEmail: String): User?

    fun findByUserIdAndUserEmail(userName: String, userEmail: String): User?


}