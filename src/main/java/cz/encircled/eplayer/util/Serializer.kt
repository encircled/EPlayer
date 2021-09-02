package cz.encircled.eplayer.util

import com.google.gson.*
import cz.encircled.eplayer.model.GenericTrackDescription
import cz.encircled.eplayer.model.PlayableMedia
import cz.encircled.eplayer.model.SingleMedia
import javafx.beans.property.SimpleLongProperty
import java.lang.reflect.Type


interface Serializer {

    fun <T> toObject(string: ByteArray, clazz: Class<T>): T = toObject(String(string), clazz)

    fun <T> toObject(string: String, clazz: Class<T>): T

    fun fromObject(obj: Any): String

}

class GsonSerializer : Serializer {

    private val gson: Gson

    init {
        val longSer: JsonSerializer<SimpleLongProperty> = JsonSerializer { t, _, ctx -> ctx.serialize(t.get()) }
        val longDes: JsonDeserializer<SimpleLongProperty> = JsonDeserializer { t, _, _ -> SimpleLongProperty(t.asLong) }

        this.gson = GsonBuilder()
            .registerTypeAdapter(PlayableMedia::class.java, JsonDeserializerWithInheritance<PlayableMedia>())
            .registerTypeAdapter(SingleMedia::class.java, JsonDeserializerWithInheritance<PlayableMedia>())
            .registerTypeAdapter(SimpleLongProperty::class.java, longSer)
            .registerTypeAdapter(SimpleLongProperty::class.java, longDes)
            .create()
    }

    override fun <T> toObject(string: String, clazz: Class<T>): T {
        return gson.fromJson(string, clazz)
    }

    override fun fromObject(obj: Any): String {
        // There is a bug with serialization of nested list
        return if (obj is MediaWrapper) "{\"media\":" + gson.toJson(obj.media) + "}"
        else gson.toJson(obj)
    }

    class JsonDeserializerWithInheritance<T> : JsonDeserializer<T> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): T {
            val jsonObject: JsonObject = json.asJsonObject
            val className: String = jsonObject.get("type").asString
            try {
                if (className.endsWith("SingleMedia")) {
                    return SingleMedia(
                        path = jsonObject["path"].asString,
                        time = SimpleLongProperty(jsonObject["time"].asLong),
                        watchDate = jsonObject["watchDate"].asLong,
                        duration = SimpleLongProperty(jsonObject["duration"].asLong),
                        preferredAudio = context.deserialize<GenericTrackDescription>(
                            jsonObject["preferredAudio"],
                            GenericTrackDescription::class.java
                        ),
                        preferredSubtitle = context.deserialize<GenericTrackDescription>(
                            jsonObject["preferredSubtitle"],
                            GenericTrackDescription::class.java
                        ),
                        metaCreationDate = jsonObject["metaCreationDate"]?.asString ?: "",
                    ) as T
                }
                return context.deserialize(jsonObject, Class.forName(className))
            } catch (e: ClassNotFoundException) {
                throw JsonParseException(e.message)
            }
        }
    }

}
