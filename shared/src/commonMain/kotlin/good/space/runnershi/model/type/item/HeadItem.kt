package good.space.runnershi.model.type.item

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.compose.resources.DrawableResource
import runnershi.shared.generated.resources.Res
import runnershi.shared.generated.resources.char_head_blue_sunglasses_0
import runnershi.shared.generated.resources.char_head_green_sunglasses_0
import runnershi.shared.generated.resources.char_head_pink_sunglasses_0
import runnershi.shared.generated.resources.char_head_red_sunglasses_0
import runnershi.shared.generated.resources.char_title_head_blue_sunglasses
import runnershi.shared.generated.resources.char_title_head_gold_band
import runnershi.shared.generated.resources.char_title_head_green_sunglasses
import runnershi.shared.generated.resources.char_title_head_pink_sunglasses
import runnershi.shared.generated.resources.char_title_head_red_sunglasses

@Serializable
@SerialName("HeadItem")
sealed class HeadItem : AvatarItem {
    override val name: String get() = this::class.simpleName ?: ""

    @Serializable
    @SerialName("HEAD_NONE")
    data object None : HeadItem() {
        @Transient override val resources: Resources? = null
        @Transient override val titleResource: DrawableResource? = null
        override val requiredLevel = 1
    }

    @Serializable
    @SerialName("RED_SUNGLASSES")
    data object RedSunglasses : HeadItem() {
        @Transient override val resources = Resources(
            Res.drawable.char_head_red_sunglasses_0,
            Res.drawable.char_head_red_sunglasses_0,
            Res.drawable.char_head_red_sunglasses_0,
            Res.drawable.char_head_red_sunglasses_0
        )
        @Transient override val titleResource = Res.drawable.char_title_head_red_sunglasses
        override val requiredLevel = 3
    }

    @Serializable
    @SerialName("BLUE_SUNGLASSES")
    data object BlueSunglasses : HeadItem() {
        @Transient override val resources = Resources(
            Res.drawable.char_head_blue_sunglasses_0,
            Res.drawable.char_head_blue_sunglasses_0,
            Res.drawable.char_head_blue_sunglasses_0,
            Res.drawable.char_head_blue_sunglasses_0
        )
        @Transient override val titleResource = Res.drawable.char_title_head_blue_sunglasses
        override val requiredLevel = 10
    }

    @Serializable
    @SerialName("PINK_SUNGLASSES")
    data object PinkSunglasses : HeadItem() {
        @Transient override val resources = Resources(
            Res.drawable.char_head_pink_sunglasses_0,
            Res.drawable.char_head_pink_sunglasses_0,
            Res.drawable.char_head_pink_sunglasses_0,
            Res.drawable.char_head_pink_sunglasses_0
        )
        @Transient override val titleResource = Res.drawable.char_title_head_pink_sunglasses
        override val requiredLevel = 15
    }

    @Serializable
    @SerialName("GREEN_SUNGLASSES")
    data object GreenSunglasses : HeadItem() {
        @Transient override val resources = Resources(
            Res.drawable.char_head_green_sunglasses_0,
            Res.drawable.char_head_green_sunglasses_0,
            Res.drawable.char_head_green_sunglasses_0,
            Res.drawable.char_head_green_sunglasses_0
        )
        @Transient override val titleResource = Res.drawable.char_title_head_green_sunglasses
        override val requiredLevel = 20
    }

    @Serializable
    @SerialName("GOLD_BAND")
    data object GoldBand : HeadItem() {
        @Transient override val resources = Resources(
            // TODO: 실제 이미지로 교체
            Res.drawable.char_head_green_sunglasses_0,
            Res.drawable.char_head_green_sunglasses_0,
            Res.drawable.char_head_green_sunglasses_0,
            Res.drawable.char_head_green_sunglasses_0
        )
        @Transient override val titleResource = Res.drawable.char_title_head_gold_band
        override val requiredLevel = 30
    }

    companion object {
        val entries: List<HeadItem> by lazy {
            listOf(
                None,
                RedSunglasses,
                BlueSunglasses,
                PinkSunglasses,
                GreenSunglasses,
                GoldBand
            )
        }

        fun valueOf(name: String): HeadItem {
            return entries.find { it.name.equals(name, ignoreCase = true) }
                ?: throw IllegalArgumentException("No BottomItem found with name $name")
        }
    }
}
