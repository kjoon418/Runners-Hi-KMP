package good.space.runnershi.model.dto.user

import good.space.runnershi.model.type.item.AvatarItem
import kotlinx.serialization.Serializable

@Serializable
data class UnlockedItem(
    val item: AvatarItem
)
