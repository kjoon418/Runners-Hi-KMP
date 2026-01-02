package good.space.runnershi.model.dto.user
import good.space.runnershi.model.domain.auth.Sex
import good.space.runnershi.model.type.item.BottomItem
import good.space.runnershi.model.type.item.HeadItem
import good.space.runnershi.model.type.item.ShoeItem
import good.space.runnershi.model.type.item.TopItem
import good.space.runnershi.ui.character.CharacterAppearance
import good.space.runnershi.ui.character.ItemType
import good.space.runnershi.ui.character.defaultResources
import kotlinx.serialization.Serializable

@Serializable
data class AvatarInfo(
    val head: HeadItem,
    val top: TopItem,
    val bottom: BottomItem,
    val shoes: ShoeItem
) {
    fun toCharacterAppearance(sex: Sex = Sex.MALE): CharacterAppearance {
        return CharacterAppearance(
            base = defaultResources(sex, ItemType.BASE) ?: emptyList(),
            head = head.resources?.let {
                listOf(it.first, it.second, it.third, it.fourth)
            },
            hair = defaultResources(sex, ItemType.HAIR),
            top = top.resources?.let {
                listOf(it.first, it.second, it.third, it.fourth)
            },
            bottom = bottom.resources?.let {
                listOf(it.first, it.second, it.third, it.fourth)
            },
            shoes = shoes.resources?.let {
                listOf(it.first, it.second, it.third, it.fourth)
            }
        )
    }
}
