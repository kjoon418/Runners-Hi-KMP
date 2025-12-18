package good.space.runnershi.global.running.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate

@Converter(autoApply = true)
class KotlinLocalDateConverter : AttributeConverter<LocalDate, java.time.LocalDate> {

    override fun convertToDatabaseColumn(attribute: LocalDate?): java.time.LocalDate? {
        return attribute?.toJavaLocalDate()
    }

    override fun convertToEntityAttribute(dbData: java.time.LocalDate?): LocalDate? {
        return dbData?.toKotlinLocalDate()
    }
}
