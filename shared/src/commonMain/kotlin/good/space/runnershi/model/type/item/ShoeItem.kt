package good.space.runnershi.model.type.item

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.compose.resources.DrawableResource
import runnershi.shared.generated.resources.Res
import runnershi.shared.generated.resources.char_shoes_blue_shoes_0
import runnershi.shared.generated.resources.char_shoes_blue_shoes_1
import runnershi.shared.generated.resources.char_shoes_blue_shoes_2
import runnershi.shared.generated.resources.char_shoes_blue_shoes_3
import runnershi.shared.generated.resources.char_shoes_orange_shoes_0
import runnershi.shared.generated.resources.char_shoes_orange_shoes_1
import runnershi.shared.generated.resources.char_shoes_orange_shoes_2
import runnershi.shared.generated.resources.char_shoes_orange_shoes_3
import runnershi.shared.generated.resources.char_shoes_red_shoes_0
import runnershi.shared.generated.resources.char_shoes_red_shoes_1
import runnershi.shared.generated.resources.char_shoes_red_shoes_2
import runnershi.shared.generated.resources.char_shoes_red_shoes_3
import runnershi.shared.generated.resources.char_title_shoes_blue_shoes
import runnershi.shared.generated.resources.char_title_shoes_orange_shoes
import runnershi.shared.generated.resources.char_title_shoes_red_shoes

@Serializable
@SerialName("ShoeItem")
sealed class ShoeItem : AvatarItem {
    override val name: String get() = this::class.simpleName ?: ""

    @Serializable
    @SerialName("SHOE_NONE")
    data object None : ShoeItem() {
        @Transient override val resources: Resources? = null
        @Transient override val titleResource: DrawableResource? = null
        override val requiredLevel = 1
    }

    @Serializable
    @SerialName("ORANGE_SHOES")
    data object OrangeShoes : ShoeItem() {
        @Transient override val resources = Resources(
            Res.drawable.char_shoes_orange_shoes_0,
            Res.drawable.char_shoes_orange_shoes_1,
            Res.drawable.char_shoes_orange_shoes_2,
            Res.drawable.char_shoes_orange_shoes_3
        )
        @Transient override val titleResource = Res.drawable.char_title_shoes_orange_shoes
        override val requiredLevel = 5
    }

    @Serializable
    @SerialName("BLUE_SHOES")
    data object BlueShoes : ShoeItem() {
        @Transient override val resources = Resources(
            Res.drawable.char_shoes_blue_shoes_0,
            Res.drawable.char_shoes_blue_shoes_1,
            Res.drawable.char_shoes_blue_shoes_2,
            Res.drawable.char_shoes_blue_shoes_3
        )
        @Transient override val titleResource = Res.drawable.char_title_shoes_blue_shoes
        override val requiredLevel = 15
    }

    @Serializable
    @SerialName("RED_SHOES")
    data object RedShoes : ShoeItem() {
        @Transient override val resources = Resources(
            Res.drawable.char_shoes_red_shoes_0,
            Res.drawable.char_shoes_red_shoes_1,
            Res.drawable.char_shoes_red_shoes_2,
            Res.drawable.char_shoes_red_shoes_3
        )
        @Transient override val titleResource = Res.drawable.char_title_shoes_red_shoes
        override val requiredLevel = 25
    }

    companion object {
        val entries: List<ShoeItem> by lazy {
            listOf(
                None,
                OrangeShoes,
                BlueShoes,
                RedShoes
            )
        }

        fun valueOf(name: String): ShoeItem {
            return entries.find { it.name.equals(name, ignoreCase = true) }
                ?: throw IllegalArgumentException("No ShoeItem found with name $name")
        }
    }
}
