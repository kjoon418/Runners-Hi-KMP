package good.space.runnershi.user.domain
import good.space.runnershi.model.type.*

import jakarta.persistence.*

@Embeddable
class UserInventory {
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "user_owned_head_items", // DB 테이블 이름
        joinColumns = [JoinColumn(name = "user_id")]
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "item_id")
    val heads: MutableSet<HeadItem> = mutableSetOf(HeadItem.NONE) // 기본 아이템 포함

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "user_owned_top_items",
        joinColumns = [JoinColumn(name = "user_id")]
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "item_id")
    val tops: MutableSet<TopItem> = mutableSetOf(TopItem.NONE)

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "user_owned_bottom_items",
        joinColumns = [JoinColumn(name = "user_id")]
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "item_id")
    val bottoms: MutableSet<BottomItem> = mutableSetOf(BottomItem.NONE)

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "user_owned_shoe_items",
        joinColumns = [JoinColumn(name = "user_id")]
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "item_id")
    val shoes: MutableSet<ShoeItem> = mutableSetOf(ShoeItem.NONE)

    fun hasHead(item: HeadItem) = heads.contains(item)
    fun hasTop(item: TopItem) = tops.contains(item)
    fun hasBottom(item: BottomItem) = bottoms.contains(item)
    fun hasShoe(item: ShoeItem) = shoes.contains(item)

    fun addHead(item: HeadItem) = heads.add(item)
    fun addTop(item: TopItem) = tops.add(item)
    fun addBottom(item: BottomItem) = bottoms.add(item)
    fun addShoe(item: ShoeItem) = shoes.add(item)
}
