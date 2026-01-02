package good.space.runnershi.user.domain

import good.space.runnershi.model.dto.user.AvatarResponse
import good.space.runnershi.model.type.item.BottomItem
import good.space.runnershi.model.type.item.HeadItem
import good.space.runnershi.model.type.item.ShoeItem
import good.space.runnershi.model.type.item.TopItem
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Transient

@Embeddable
class Avatar(
    @Column(name = "head")
    private var _head: String = HeadItem.None.name,

    @Column(name = "top")
    private var _top: String = TopItem.None.name,

    @Column(name = "bottom")
    private var _bottom: String = BottomItem.None.name,

    @Column(name = "shoes")
    private var _shoes: String = ShoeItem.None.name
) {
    var head: HeadItem
        @Transient get() = try { HeadItem.valueOf(_head) } catch (_: Exception) { HeadItem.None }
        set(value) { _head = value.name }

    var top: TopItem
        @Transient get() = try { TopItem.valueOf(_top) } catch (_: Exception) { TopItem.None }
        set(value) { _top = value.name }

    var bottom: BottomItem
        @Transient get() = try { BottomItem.valueOf(_bottom) } catch (_: Exception) { BottomItem.None }
        set(value) { _bottom = value.name }

    var shoes: ShoeItem
        @Transient get() = try { ShoeItem.valueOf(_shoes) } catch (_: Exception) { ShoeItem.None }
        set(value) { _shoes = value.name }

    fun toResponse(): AvatarResponse {
        return AvatarResponse(
            head = this.head,
            top = this.top,
            bottom = this.bottom,
            shoes = this.shoes
        )
    }
}
