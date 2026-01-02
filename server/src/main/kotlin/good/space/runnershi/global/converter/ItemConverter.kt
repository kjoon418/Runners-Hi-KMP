package good.space.runnershi.global.converter

import good.space.runnershi.model.type.item.BottomItem
import good.space.runnershi.model.type.item.HeadItem
import good.space.runnershi.model.type.item.ShoeItem
import good.space.runnershi.model.type.item.TopItem
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class HeadItemConverter : AttributeConverter<HeadItem, String> {

    override fun convertToDatabaseColumn(attribute: HeadItem?): String? {
        return attribute?.name
    }

    override fun convertToEntityAttribute(dbData: String?): HeadItem {
        return if (dbData.isNullOrEmpty()) {
            HeadItem.None
        } else {
            try {
                HeadItem.valueOf(dbData)
            } catch (_: IllegalArgumentException) {
                HeadItem.None
            }
        }
    }
}

@Converter(autoApply = true)
class TopItemConverter : AttributeConverter<TopItem, String> {
    override fun convertToDatabaseColumn(attribute: TopItem?): String? {
        return attribute?.name
    }

    override fun convertToEntityAttribute(dbData: String?): TopItem {
        return if (dbData.isNullOrEmpty()) {
            TopItem.None
        } else {
            try {
                TopItem.valueOf(dbData)
            } catch (_: Exception) {
                TopItem.None
            }
        }
    }
}

@Converter(autoApply = true)
class BottomItemConverter : AttributeConverter<BottomItem, String> {
    override fun convertToDatabaseColumn(attribute: BottomItem?): String? {
        return attribute?.name
    }

    override fun convertToEntityAttribute(dbData: String?): BottomItem {
        return if (dbData.isNullOrEmpty()) {
            BottomItem.None
        } else {
            try {
                BottomItem.valueOf(dbData)
            } catch (_: Exception) {
                BottomItem.None
            }
        }
    }
}

@Converter(autoApply = true)
class ShoeItemConverter : AttributeConverter<ShoeItem, String> {
    override fun convertToDatabaseColumn(attribute: ShoeItem?): String? {
        return attribute?.name
    }

    override fun convertToEntityAttribute(dbData: String?): ShoeItem {
        return if (dbData.isNullOrEmpty()) {
            ShoeItem.None
        } else {
            try {
                ShoeItem.valueOf(dbData)
            } catch (e: Exception) {
                ShoeItem.None
            }
        }
    }
}
