package good.space.runnershi.model.type.item

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.compose.resources.DrawableResource
import runnershi.shared.generated.resources.Res
import runnershi.shared.generated.resources.char_bottom_green_shorts_0
import runnershi.shared.generated.resources.char_bottom_green_shorts_1
import runnershi.shared.generated.resources.char_bottom_green_shorts_2
import runnershi.shared.generated.resources.char_bottom_green_shorts_3
import runnershi.shared.generated.resources.char_bottom_pink_shorts_0
import runnershi.shared.generated.resources.char_bottom_pink_shorts_1
import runnershi.shared.generated.resources.char_bottom_pink_shorts_2
import runnershi.shared.generated.resources.char_bottom_pink_shorts_3
import runnershi.shared.generated.resources.char_bottom_white_shorts_0
import runnershi.shared.generated.resources.char_bottom_white_shorts_1
import runnershi.shared.generated.resources.char_bottom_white_shorts_2
import runnershi.shared.generated.resources.char_bottom_white_shorts_3
import runnershi.shared.generated.resources.char_title_bottom_green_shorts
import runnershi.shared.generated.resources.char_title_bottom_pink_shorts
import runnershi.shared.generated.resources.char_title_bottom_white_shorts

@Serializable
@SerialName("BottomItem")
sealed class BottomItem : AvatarItem {
    override val name: String get() = this::class.simpleName ?: ""

    @Serializable
    @SerialName("BOTTOM_NONE")
    data object None : BottomItem() {
        @Transient
        override val resources: Resources? = null
        @Transient override val titleResource: DrawableResource? = null
        override val requiredLevel = 1
    }

    @Serializable
    @SerialName("PINK_SHORTS")
    data object PinkShorts : BottomItem() {
        @Transient override val resources = Resources(
            Res.drawable.char_bottom_pink_shorts_0,
            Res.drawable.char_bottom_pink_shorts_1,
            Res.drawable.char_bottom_pink_shorts_2,
            Res.drawable.char_bottom_pink_shorts_3
        )
        @Transient override val titleResource = Res.drawable.char_title_bottom_pink_shorts
        override val requiredLevel = 10
    }

    @Serializable
    @SerialName("GREEN_SHORTS")
    data object GreenShorts : BottomItem() {
        @Transient override val resources = Resources(
            Res.drawable.char_bottom_green_shorts_0,
            Res.drawable.char_bottom_green_shorts_1,
            Res.drawable.char_bottom_green_shorts_2,
            Res.drawable.char_bottom_green_shorts_3
        )
        @Transient override val titleResource = Res.drawable.char_title_bottom_green_shorts
        override val requiredLevel = 28
    }

    @Serializable
    @SerialName("WHITE_SHORTS")
    data object WhiteShorts : BottomItem() {
        @Transient override val resources = Resources(
            Res.drawable.char_bottom_white_shorts_0,
            Res.drawable.char_bottom_white_shorts_1,
            Res.drawable.char_bottom_white_shorts_2,
            Res.drawable.char_bottom_white_shorts_3
        )
        @Transient override val titleResource = Res.drawable.char_title_bottom_white_shorts
        override val requiredLevel = 25
    }

    companion object {
        val entries: List<BottomItem> by lazy {
            listOf(
                None,
                PinkShorts,
                GreenShorts,
                WhiteShorts
            )
        }

        fun valueOf(name: String): BottomItem {
            return entries.find { it.name.equals(name, ignoreCase = true) }
                ?: throw IllegalArgumentException("No BottomItem found with name $name")
        }
    }
}
