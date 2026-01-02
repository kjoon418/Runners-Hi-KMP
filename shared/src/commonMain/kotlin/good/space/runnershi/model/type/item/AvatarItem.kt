package good.space.runnershi.model.type.item

import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource

@Serializable
sealed interface AvatarItem {
    val resources: Resources?
    val titleResource: DrawableResource?
    val requiredLevel: Int
    val name: String
}
