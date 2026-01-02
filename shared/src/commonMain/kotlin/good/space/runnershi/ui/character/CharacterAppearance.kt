package good.space.runnershi.ui.character

import good.space.runnershi.model.domain.auth.Sex
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

val defaultCharacterAppearance = CharacterAppearance(
    head = defaultResources(Sex.MALE, ItemType.HEAD),
    hair = defaultResources(Sex.MALE, ItemType.HAIR),
    top = defaultResources(Sex.MALE, ItemType.TOP),
    bottom = defaultResources(Sex.MALE, ItemType.BOTTOM),
    shoes = defaultResources(Sex.MALE, ItemType.SHOES)
)
