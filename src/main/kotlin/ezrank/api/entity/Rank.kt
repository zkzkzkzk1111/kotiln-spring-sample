package ezrank.api.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.time.ZoneId

@Entity
@Table(name = "`rank`")
class Rank(


    @Column(name = "keyword_idx", nullable = false)
    var keywordIdx: Int,

    @Column(name = "rank_name", nullable = false)
    var rankName: String,

    @Column(name = "rank_num", nullable = false)
    var rankNum: Int,

    @Column(name = "user_idx", nullable = false)
    var userIdx: Int,

    @Column(name = "rank_date", nullable = false)
    var rankDate: LocalDateTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rank_idx")
    var rankIdx: Int? = null

    constructor() : this(0, "", 0, 0, LocalDateTime.now(ZoneId.of("Asia/Seoul")))
}