package good.space.runnershi.user.domain

import good.space.runnershi.model.type.item.BottomItem
import good.space.runnershi.model.type.item.HeadItem
import good.space.runnershi.model.type.item.ShoeItem
import good.space.runnershi.model.type.item.TopItem
import jakarta.persistence.*

@Embeddable
class UserInventory {

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "user_owned_head_items",
        joinColumns = [JoinColumn(name = "user_id")]
    )
    @Column(name = "item_id")
    private val _headNames: MutableSet<String> = mutableSetOf(HeadItem.None.name)

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "user_owned_top_items",
        joinColumns = [JoinColumn(name = "user_id")]
    )
    @Column(name = "item_id")
    private val _topNames: MutableSet<String> = mutableSetOf(TopItem.None.name)

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "user_owned_bottom_items",
        joinColumns = [JoinColumn(name = "user_id")]
    )
    @Column(name = "item_id")
    private val _bottomNames: MutableSet<String> = mutableSetOf(BottomItem.None.name)

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "user_owned_shoe_items",
        joinColumns = [JoinColumn(name = "user_id")]
    )
    @Column(name = "item_id")
    private val _shoeNames: MutableSet<String> = mutableSetOf(ShoeItem.None.name)

    fun hasHead(item: HeadItem) = _headNames.contains(item.name)
    fun hasTop(item: TopItem) = _topNames.contains(item.name)
    fun hasBottom(item: BottomItem) = _bottomNames.contains(item.name)
    fun hasShoe(item: ShoeItem) = _shoeNames.contains(item.name)

    fun addHead(item: HeadItem) { _headNames.add(item.name) }
    fun addTop(item: TopItem) { _topNames.add(item.name) }
    fun addBottom(item: BottomItem) { _bottomNames.add(item.name) }
    fun addShoe(item: ShoeItem) { _shoeNames.add(item.name) }
}
