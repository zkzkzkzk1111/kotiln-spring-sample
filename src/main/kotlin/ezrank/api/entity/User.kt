package ezrank.api.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "`user`",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id"]),
        UniqueConstraint(columnNames = ["user_email"])
    ]
)
class User(
    @Column(name = "user_id", nullable = false, unique = true)
    var userId: String,

    @Column(name = "user_password", nullable = false)
    var userPassword: String,

    @Column(name = "user_email", nullable = false, unique = true)
    var userEmail: String,

    @Column(name = "user_name", nullable = false)
    var userName: String,

    @Column(name = "is_agree", nullable = false)
    var isAgree: Boolean = true,

    @Column(name = "marketing_agree", nullable = false)
    var marketingAgree: Boolean = false,

    @Column(name = "user_write_date", nullable = false)
    var userWriteDate: LocalDateTime = LocalDateTime.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_idx")
    var userIdx: Long? = null

    // JPA용 기본 생성자
    protected constructor() : this("", "", "", "", true, false, LocalDateTime.now())

    @PrePersist
    protected fun onCreate() {
        if (userWriteDate == null) {
            userWriteDate = LocalDateTime.now()
        }
    }
}