package good.space.runnershi.model.dto.user
import good.space.runnershi.model.type.*
import kotlinx.serialization.Serializable

@Serializable
data class AvatarUpdateRequest(
    val head: HeadItem,
    val top: TopItem,
    val bottom: BottomItem,
    val shoes: ShoeItem
)
