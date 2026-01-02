package good.space.runnershi.model.dto.user
import good.space.runnershi.model.type.item.BottomItem
import good.space.runnershi.model.type.item.HeadItem
import good.space.runnershi.model.type.item.ShoeItem
import good.space.runnershi.model.type.item.TopItem
import kotlinx.serialization.Serializable

@Serializable
data class AvatarUpdateRequest(
    val head: HeadItem,
    val top: TopItem,
    val bottom: BottomItem,
    val shoes: ShoeItem
)
