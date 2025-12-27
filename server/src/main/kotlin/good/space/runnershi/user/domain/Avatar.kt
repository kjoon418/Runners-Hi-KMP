package good.space.runnershi.user.domain

import good.space.runnershi.model.type.* // shared의 Enum 임포트
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
class Avatar(
    @Enumerated(EnumType.STRING)
    var head: HeadItem = HeadItem.NONE,

    @Enumerated(EnumType.STRING)
    var face: FaceItem = FaceItem.NONE,

    @Enumerated(EnumType.STRING)
    var top: TopItem = TopItem.NONE,

    @Enumerated(EnumType.STRING)
    var bottom: BottomItem = BottomItem.NONE,

    @Enumerated(EnumType.STRING)
    var shoes: ShoeItem = ShoeItem.NONE
)
