package ezrank.api.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "place")
class Place(
    @Column(name = "user_idx", nullable = false)
    var userIdx: Int,

    @Column(name = "place_id", nullable = false)
    var placeId: Int,

    @Column(name = "place_date", nullable = false)
    var placeDate: LocalDateTime = LocalDateTime.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_idx")
    var placeIdx: Int? = null

    // JPA용 기본 생성자
    constructor() : this(0, 0, LocalDateTime.now())
}
