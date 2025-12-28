package good.space.runnershi.ui.character

import good.space.runnershi.model.domain.auth.Sex
import org.jetbrains.compose.resources.DrawableResource
import runnershi.shared.generated.resources.Res
import runnershi.shared.generated.resources.*

fun defaultResources(sex: Sex, type: ItemType): List<DrawableResource>? {
    return when (sex) {
        Sex.MALE -> maleDefaultResources(type)
        Sex.FEMALE -> femaleDefaultResources(type)
    }
}

private fun maleDefaultResources(type: ItemType): List<DrawableResource>? {
    return when (type) {
        ItemType.HEAD -> null
        ItemType.HAIR -> listOf(
            Res.drawable.char_hair_man_0,
            Res.drawable.char_hair_man_1,
            Res.drawable.char_hair_man_2,
            Res.drawable.char_hair_man_3
        )
        ItemType.TOP -> listOf(
            Res.drawable.char_top_man_0,
            Res.drawable.char_top_man_1,
            Res.drawable.char_top_man_2,
            Res.drawable.char_top_man_3
        )
        ItemType.BOTTOM -> listOf(
            Res.drawable.char_bottom_man_0,
            Res.drawable.char_bottom_man_1,
            Res.drawable.char_bottom_man_2,
            Res.drawable.char_bottom_man_3
        )
        ItemType.SHOES -> listOf(
            Res.drawable.char_shoes_man_0,
            Res.drawable.char_shoes_man_1,
            Res.drawable.char_shoes_man_2,
            Res.drawable.char_shoes_man_3
        )
        ItemType.BASE -> listOf(
            Res.drawable.char_base_default_0,
            Res.drawable.char_base_default_1,
            Res.drawable.char_base_default_2,
            Res.drawable.char_base_default_3
        )
    }
}

private fun femaleDefaultResources(type: ItemType): List<DrawableResource>? {
    return when (type) {
        ItemType.HEAD -> null
        ItemType.HAIR -> listOf(
            Res.drawable.char_hair_woman_0,
            Res.drawable.char_hair_woman_1,
            Res.drawable.char_hair_woman_2,
            Res.drawable.char_hair_woman_3
        )
        ItemType.TOP -> listOf(
            Res.drawable.char_top_woman_0,
            Res.drawable.char_top_woman_1,
            Res.drawable.char_top_woman_2,
            Res.drawable.char_top_woman_3
        )
        ItemType.BOTTOM -> listOf(
            Res.drawable.char_bottom_woman_0,
            Res.drawable.char_bottom_woman_1,
            Res.drawable.char_bottom_woman_2,
            Res.drawable.char_bottom_woman_3
        )
        ItemType.SHOES -> listOf(
            Res.drawable.char_shoes_woman_0,
            Res.drawable.char_shoes_woman_1,
            Res.drawable.char_shoes_woman_2,
            Res.drawable.char_shoes_woman_3
        )
        ItemType.BASE -> listOf(
            Res.drawable.char_base_default_0,
            Res.drawable.char_base_default_1,
            Res.drawable.char_base_default_2,
            Res.drawable.char_base_default_3
        )
    }
}
