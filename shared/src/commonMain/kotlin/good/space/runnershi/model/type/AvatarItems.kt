package good.space.runnershi.model.type

import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import runnershi.shared.generated.resources.Res
// 모든 리소스를 가져오기 위해 와일드카드 import 사용
import runnershi.shared.generated.resources.*

data class Resources(
    val first: DrawableResource,
    val second: DrawableResource,
    val third: DrawableResource,
    val fourth: DrawableResource
)

sealed interface AvatarItem {
    val resources: Resources?
    val titleResource: DrawableResource?
    val requiredLevel: Int
    val name: String
}

@Serializable
enum class HeadItem(
    override val resources: Resources?,
    override val titleResource: DrawableResource?,
    override val requiredLevel: Int
) : AvatarItem {
    NONE(
        resources = null,
        titleResource = null,
        requiredLevel = 1
    ),
    RED_SUNGLASSES(
        resources = Resources(
            Res.drawable.char_head_red_sunglasses_0,
            Res.drawable.char_head_red_sunglasses_0,
            Res.drawable.char_head_red_sunglasses_0,
            Res.drawable.char_head_red_sunglasses_0
        ),
        titleResource = Res.drawable.char_title_head_red_sunglasses,
        requiredLevel = 3
    ),
    BLUE_SUNGLASSES(
        resources = Resources(
            Res.drawable.char_head_blue_sunglasses_0,
            Res.drawable.char_head_blue_sunglasses_0,
            Res.drawable.char_head_blue_sunglasses_0,
            Res.drawable.char_head_blue_sunglasses_0
        ),
        titleResource = Res.drawable.char_title_head_blue_sunglasses,
        requiredLevel = 10
    ),
    PINK_SUNGLASSES(
        resources = Resources(
            Res.drawable.char_head_pink_sunglasses_0,
            Res.drawable.char_head_pink_sunglasses_0,
            Res.drawable.char_head_pink_sunglasses_0,
            Res.drawable.char_head_pink_sunglasses_0
        ),
        titleResource = Res.drawable.char_title_head_pink_sunglasses,
        requiredLevel = 15
    ),
    GREEN_SUNGLASSES(
        resources = Resources(
            Res.drawable.char_head_green_sunglasses_0,
            Res.drawable.char_head_green_sunglasses_0,
            Res.drawable.char_head_green_sunglasses_0,
            Res.drawable.char_head_green_sunglasses_0
        ),
        titleResource = Res.drawable.char_title_head_green_sunglasses,
        requiredLevel = 20
    ),
    GOLD_BAND(
        resources = Resources(
            // TODO: 실제 이미지로 교체
            Res.drawable.char_head_green_sunglasses_0,
            Res.drawable.char_head_green_sunglasses_0,
            Res.drawable.char_head_green_sunglasses_0,
            Res.drawable.char_head_green_sunglasses_0
        ),
        titleResource = Res.drawable.char_title_head_gold_band,
        requiredLevel = 30
    )
}

@Serializable
enum class TopItem(
    override val resources: Resources?,
    override val titleResource: DrawableResource?,
    override val requiredLevel: Int
) : AvatarItem {
    NONE(
        resources = null,
        titleResource = null,
        requiredLevel = 1
    ),
    PINK_VEST(
        resources = Resources(
            Res.drawable.char_top_pink_vest_0,
            Res.drawable.char_top_pink_vest_1,
            Res.drawable.char_top_pink_vest_2,
            Res.drawable.char_top_pink_vest_3
        ),
        titleResource = Res.drawable.char_title_top_pink_vest,
        requiredLevel = 5
    ),
    GREEN_VEST(
        resources = Resources(
            Res.drawable.char_top_green_vest_0,
            Res.drawable.char_top_green_vest_1,
            Res.drawable.char_top_green_vest_2,
            Res.drawable.char_top_green_vest_3
        ),
        titleResource = Res.drawable.char_title_top_green_vest,
        requiredLevel = 15
    ),
    WHITE_VEST(
        resources = Resources(
            Res.drawable.char_top_white_vest_0,
            Res.drawable.char_top_white_vest_1,
            Res.drawable.char_top_white_vest_2,
            Res.drawable.char_top_white_vest_3
        ),
        titleResource = Res.drawable.char_title_top_white_vest,
        requiredLevel = 30
    )
}

@Serializable
enum class BottomItem(
    override val resources: Resources?,
    override val titleResource: DrawableResource?,
    override val requiredLevel: Int
) : AvatarItem {
    NONE(
        resources = null,
        titleResource = null,
        requiredLevel = 1
    ),
    PINK_SHORTS(
        resources = Resources(
            Res.drawable.char_bottom_pink_shorts_0,
            Res.drawable.char_bottom_pink_shorts_1,
            Res.drawable.char_bottom_pink_shorts_2,
            Res.drawable.char_bottom_pink_shorts_3
        ),
        titleResource = Res.drawable.char_title_bottom_pink_shorts,
        requiredLevel = 10
    ),
    GREEN_SHORTS(
        resources = Resources(
            Res.drawable.char_bottom_green_shorts_0,
            Res.drawable.char_bottom_green_shorts_1,
            Res.drawable.char_bottom_green_shorts_2,
            Res.drawable.char_bottom_green_shorts_3
        ),
        titleResource = Res.drawable.char_title_bottom_green_shorts,
        requiredLevel = 28
    ),
    WHITE_SHORTS(
        resources = Resources(
            Res.drawable.char_bottom_white_shorts_0,
            Res.drawable.char_bottom_white_shorts_1,
            Res.drawable.char_bottom_white_shorts_2,
            Res.drawable.char_bottom_white_shorts_3
        ),
        titleResource = Res.drawable.char_title_bottom_white_shorts,
        requiredLevel = 25
    )
}

@Serializable
enum class ShoeItem(
    override val resources: Resources?,
    override val titleResource: DrawableResource?,
    override val requiredLevel: Int
) : AvatarItem {
    NONE(
        resources = null,
        titleResource = null,
        requiredLevel = 1
    ),
    ORANGE_SHOES(
        resources = Resources(
            Res.drawable.char_shoes_orange_shoes_0,
            Res.drawable.char_shoes_orange_shoes_1,
            Res.drawable.char_shoes_orange_shoes_2,
            Res.drawable.char_shoes_orange_shoes_3
        ),
        titleResource = Res.drawable.char_title_shoes_orange_shoes,
        requiredLevel = 5
    ),
    BLUE_SHOES(
        resources = Resources(
            Res.drawable.char_shoes_blue_shoes_0,
            Res.drawable.char_shoes_blue_shoes_1,
            Res.drawable.char_shoes_blue_shoes_2,
            Res.drawable.char_shoes_blue_shoes_3
        ),
        titleResource = Res.drawable.char_title_shoes_blue_shoes,
        requiredLevel = 15
    ),
    RED_SHOES(
        resources = Resources(
            Res.drawable.char_shoes_red_shoes_0,
            Res.drawable.char_shoes_red_shoes_1,
            Res.drawable.char_shoes_red_shoes_2,
            Res.drawable.char_shoes_red_shoes_3
        ),
        titleResource = Res.drawable.char_title_shoes_red_shoes,
        requiredLevel = 25
    )
}
