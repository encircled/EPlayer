package cz.encircled.eplayer.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import cz.encircled.eplayer.model.PlayableMedia
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty

object JsonMapper {

    fun getMapper(): ObjectMapper {
        val stringProperty = SimpleModule("StringModule", Version(1, 0, 0, null))
        stringProperty.addSerializer(SimpleStringProperty::class.java, SimpleStringPropertySerializer())
        stringProperty.addDeserializer(SimpleStringProperty::class.java, SimpleStringPropertyDeserializer())

        val longProperty = SimpleModule("LongModule", Version(1, 0, 0, null))
        longProperty.addSerializer(SimpleLongProperty::class.java, SimpleLongPropertySerializer())
        longProperty.addDeserializer(SimpleLongProperty::class.java, SimpleLongPropertyDeserializer())

        return ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModules(KotlinModule(nullIsSameAsDefault = true), longProperty, stringProperty)
            .activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                    .allowIfBaseType(PlayableMedia::class.java)
                    .build()
            )
    }

    class SimpleStringPropertySerializer : StdSerializer<SimpleStringProperty>(SimpleStringProperty::class.java) {
        override fun serialize(p0: SimpleStringProperty, jgen: JsonGenerator, p2: SerializerProvider?) {
            jgen.writeString(p0.get())
        }
    }

    class SimpleStringPropertyDeserializer : StdDeserializer<SimpleStringProperty>(SimpleStringProperty::class.java) {
        override fun deserialize(p0: JsonParser, p1: DeserializationContext?): SimpleStringProperty {
            return SimpleStringProperty(p0.readValueAs(String::class.java))
        }
    }


    class SimpleLongPropertySerializer : StdSerializer<SimpleLongProperty>(SimpleLongProperty::class.java) {
        override fun serialize(p0: SimpleLongProperty, jgen: JsonGenerator, p2: SerializerProvider?) {
            jgen.writeNumber(p0.get())
        }
    }

    class SimpleLongPropertyDeserializer : StdDeserializer<SimpleLongProperty>(SimpleLongProperty::class.java) {
        override fun deserialize(p0: JsonParser, p1: DeserializationContext?): SimpleLongProperty {
            return SimpleLongProperty(p0.readValueAs(Long::class.java))
        }
    }


}
