package good.space.runnershi.model.type.item

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.compose.resources.DrawableResource
import runnershi.shared.generated.resources.Res
import runnershi.shared.generated.resources.char_title_top_green_vest
import runnershi.shared.generated.resources.char_title_top_pink_vest
import runnershi.shared.generated.resources.char_title_top_white_vest
import runnershi.shared.generated.resources.char_top_green_vest_0
import runnershi.shared.generated.resources.char_top_green_vest_1
import runnershi.shared.generated.resources.char_top_green_vest_2
import runnershi.shared.generated.resources.char_top_green_vest_3
import runnershi.shared.generated.resources.char_top_pink_vest_0
import runnershi.shared.generated.resources.char_top_pink_vest_1
import runnershi.shared.generated.resources.char_top_pink_vest_2
import runnershi.shared.generated.resources.char_top_pink_vest_3
import runnershi.shared.generated.resources.char_top_white_vest_0
import runnershi.shared.generated.resources.char_top_white_vest_1
import runnershi.shared.generated.resources.char_top_white_vest_2
import runnershi.shared.generated.resources.char_top_white_vest_3

@Serializable
@SerialName("TopItem")
sealed class TopItem : AvatarItem {
    override val name: String get() = this::class.simpleName ?: ""

    @Serializable
    @SerialName("TOP_NONE")
    data object None : TopItem() {
        @Transient
        override val resources: Resources? = null
        @Transient override val titleResource: DrawableResource? = null
        override val requiredLevel = 1
    }

    @Serializable
    @SerialName("PINK_VEST")
    data object PinkVest : TopItem() {
        @Transient override val resources = Resources(
            Res.drawable.char_top_pink_vest_0,
            Res.drawable.char_top_pink_vest_1,
            Res.drawable.char_top_pink_vest_2,
            Res.drawable.char_top_pink_vest_3
        )
        @Transient override val titleResource = Res.drawable.char_title_top_pink_vest
        override val requiredLevel = 5
    }

    @Serializable
    @SerialName("GREEN_VEST")
    data object GreenVest : TopItem() {
        @Transient override val resources = Resources(
            Res.drawable.char_top_green_vest_0,
            Res.drawable.char_top_green_vest_1,
            Res.drawable.char_top_green_vest_2,
            Res.drawable.char_top_green_vest_3
        )
        @Transient override val titleResource = Res.drawable.char_title_top_green_vest
        override val requiredLevel = 15
    }

    @Serializable
    @SerialName("WHITE_VEST")
    data object WhiteVest : TopItem() {
        @Transient override val resources = Resources(
            Res.drawable.char_top_white_vest_0,
            Res.drawable.char_top_white_vest_1,
            Res.drawable.char_top_white_vest_2,
            Res.drawable.char_top_white_vest_3
        )
        @Transient override val titleResource = Res.drawable.char_title_top_white_vest
        override val requiredLevel = 30
    }

    companion object {
        val entries: List<TopItem> by lazy {
            listOf(
                None,
                PinkVest,
                GreenVest,
                WhiteVest
            )
        }

        fun valueOf(name: String): TopItem {
            return entries.find { it.name.equals(name, ignoreCase = true) }
                ?: throw IllegalArgumentException("No TopItem found with name $name")
        }
    }
}
