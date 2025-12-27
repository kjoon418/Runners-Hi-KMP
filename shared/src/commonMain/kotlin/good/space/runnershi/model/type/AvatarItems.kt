package good.space.runnershi.model.type

import kotlinx.serialization.Serializable


@Serializable
enum class HeadItem {
    NONE,
    RED_SUNGLASSES,
    BLUE_SUNGLASSES,
    PINK_SUNGLASSES,
    GREEN_SUNGLASSES,
    GOLD_BAND
}

@Serializable
enum class TopItem {
    NONE,
    PINK_VEST,
    GREEN_VEST,
    WHITE_VEST
}

@Serializable
enum class BottomItem {
    NONE,
    PINK_SHORTS,
    GREEN_SHORTS,
    WHITE_SHORTS
}

@Serializable
enum class ShoeItem {
    NONE,
    ORANGE_SHOES,
    BLUE_SHOES,
    RED_SHOES
}
