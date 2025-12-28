package good.space.runnershi.model.dto.user

import good.space.runnershi.model.type.BottomItem
import good.space.runnershi.model.type.HeadItem
import good.space.runnershi.model.type.ShoeItem
import good.space.runnershi.model.type.TopItem
import kotlinx.serialization.Serializable

@Serializable
class AvatarResponse (
    val head: HeadItem,
    val top: TopItem,
    val bottom: BottomItem,
    val shoes: ShoeItem
)



