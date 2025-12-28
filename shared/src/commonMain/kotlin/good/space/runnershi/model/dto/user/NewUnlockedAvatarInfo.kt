package good.space.runnershi.model.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class NewUnlockedAvatarInfo(
    val category: String,
    val itemName: String
)
