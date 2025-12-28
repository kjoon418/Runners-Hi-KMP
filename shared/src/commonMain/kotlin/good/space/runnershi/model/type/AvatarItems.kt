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

// 편의를 위해 리스트로 변환하는 확장 함수 추가 (선택 사항)
fun Resources.toList(): List<DrawableResource> = listOf(first, second, third, fourth)

@Serializable
enum class HeadItem(
    val resources: Resources?
) {
    NONE(null),
    RED_SUNGLASSES(
        Resources(
            Res.drawable.char_head_red_sunglasses_0,
            Res.drawable.char_head_red_sunglasses_0,
            Res.drawable.char_head_red_sunglasses_0,
            Res.drawable.char_head_red_sunglasses_0
        )
    ),
    BLUE_SUNGLASSES(
        Resources(
            Res.drawable.char_head_blue_sunglasses_0,
            Res.drawable.char_head_blue_sunglasses_0,
            Res.drawable.char_head_blue_sunglasses_0,
            Res.drawable.char_head_blue_sunglasses_0
        )
    ),
    PINK_SUNGLASSES(
        Resources(
            Res.drawable.char_head_pink_sunglasses_0,
            Res.drawable.char_head_pink_sunglasses_0,
            Res.drawable.char_head_pink_sunglasses_0,
            Res.drawable.char_head_pink_sunglasses_0
        )
    ),
    GREEN_SUNGLASSES(
        Resources(
            Res.drawable.char_head_green_sunglasses_0,
            Res.drawable.char_head_green_sunglasses_0,
            Res.drawable.char_head_green_sunglasses_0,
            Res.drawable.char_head_green_sunglasses_0
        )
    ),
    GOLD_BAND(
        Resources(
            // TODO: 실제 이미지로 교체
            Res.drawable.char_head_green_sunglasses_0,
            Res.drawable.char_head_green_sunglasses_0,
            Res.drawable.char_head_green_sunglasses_0,
            Res.drawable.char_head_green_sunglasses_0
        )
    )
}

@Serializable
enum class TopItem(
    val resources: Resources?
) {
    NONE(null),
    PINK_VEST(
        Resources(
            Res.drawable.char_top_pink_vest_0,
            Res.drawable.char_top_pink_vest_1,
            Res.drawable.char_top_pink_vest_2,
            Res.drawable.char_top_pink_vest_3
        )
    ),
    GREEN_VEST(
        Resources(
            Res.drawable.char_top_green_vest_0,
            Res.drawable.char_top_green_vest_1,
            Res.drawable.char_top_green_vest_2,
            Res.drawable.char_top_green_vest_3
        )
    ),
    WHITE_VEST(
        Resources(
            Res.drawable.char_top_white_vest_0,
            Res.drawable.char_top_white_vest_1,
            Res.drawable.char_top_white_vest_2,
            Res.drawable.char_top_white_vest_3
        )
    )
}

@Serializable
enum class BottomItem(
    val resources: Resources?
) {
    NONE(null),
    PINK_SHORTS(
        Resources(
            Res.drawable.char_bottom_pink_shorts_0,
            Res.drawable.char_bottom_pink_shorts_1,
            Res.drawable.char_bottom_pink_shorts_2,
            Res.drawable.char_bottom_pink_shorts_3
        )
    ),
    GREEN_SHORTS(
        Resources(
            Res.drawable.char_bottom_green_shorts_0,
            Res.drawable.char_bottom_green_shorts_1,
            Res.drawable.char_bottom_green_shorts_2,
            Res.drawable.char_bottom_green_shorts_3
        )
    ),
    WHITE_SHORTS(
        Resources(
            Res.drawable.char_bottom_white_shorts_0,
            Res.drawable.char_bottom_white_shorts_1,
            Res.drawable.char_bottom_white_shorts_2,
            Res.drawable.char_bottom_white_shorts_3
        )
    )
}

@Serializable
enum class ShoeItem(
    val resources: Resources?
) {
    NONE(null),
    ORANGE_SHOES(
        Resources(
            Res.drawable.char_shoes_orange_shoes_0,
            Res.drawable.char_shoes_orange_shoes_1,
            Res.drawable.char_shoes_orange_shoes_2,
            Res.drawable.char_shoes_orange_shoes_3
        )
    ),
    BLUE_SHOES(
        Resources(
            Res.drawable.char_shoes_blue_shoes_0,
            Res.drawable.char_shoes_blue_shoes_1,
            Res.drawable.char_shoes_blue_shoes_2,
            Res.drawable.char_shoes_blue_shoes_3
        )
    ),
    RED_SHOES(
        Resources(
            Res.drawable.char_shoes_red_shoes_0,
            Res.drawable.char_shoes_red_shoes_1,
            Res.drawable.char_shoes_red_shoes_2,
            Res.drawable.char_shoes_red_shoes_3
        )
    )
}
