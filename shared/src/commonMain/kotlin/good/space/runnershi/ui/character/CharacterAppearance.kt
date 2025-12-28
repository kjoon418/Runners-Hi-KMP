package good.space.runnershi.ui.character

import org.jetbrains.compose.resources.DrawableResource

import runnershi.shared.generated.resources.Res.drawable
import runnershi.shared.generated.resources.char_base_default_0
import runnershi.shared.generated.resources.char_base_default_1
import runnershi.shared.generated.resources.char_base_default_2
import runnershi.shared.generated.resources.char_base_default_3

data class CharacterAppearance(
    val base: List<DrawableResource> = listOf(
        drawable.char_base_default_0,
        drawable.char_base_default_1,
        drawable.char_base_default_2,
        drawable.char_base_default_3
    ),
    val head: List<DrawableResource>? = null,
    val hair: List<DrawableResource>? = null,
    val top: List<DrawableResource>? = null,
    val bottom: List<DrawableResource>? = null,
    val shoes: List<DrawableResource>? = null
)
