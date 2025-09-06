package ezrank.api.entity


import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "keyword")
class Keyword(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyword_idx")
    var keywordIdx: Int? = null,

    @Column(name = "place_idx", nullable = false)
    var placeIdx: Int,

    @Column(name = "keyword_name", nullable = false)
    var keywordName: String,

    @Column(name = "keyword_date", nullable = false)
    var keywordDate: LocalDateTime = LocalDateTime.now()
) {
    constructor() : this(null, 0, "", LocalDateTime.now())
}
