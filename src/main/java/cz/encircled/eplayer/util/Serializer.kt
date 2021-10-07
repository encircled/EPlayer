package cz.encircled.eplayer.util

import com.google.gson.*
import javafx.beans.property.SimpleLongProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.lang.reflect.ParameterizedType


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
        val obsDes: JsonDeserializer<ObservableList<*>> = JsonDeserializer { t, type, ctx ->
            FXCollections.observableArrayList(
                (t as JsonArray).map { ctx.deserialize<Any>(it, (type as ParameterizedType).actualTypeArguments[0]) }
            )
        }

        this.gson = GsonBuilder()
            .registerTypeAdapter(SimpleLongProperty::class.java, longSer)
            .registerTypeAdapter(SimpleLongProperty::class.java, longDes)
            .registerTypeAdapter(ObservableList::class.java, obsDes)
            .create()
    }

    override fun <T> toObject(string: String, clazz: Class<T>): T = gson.fromJson(string, clazz)

    override fun fromObject(obj: Any): String = gson.toJson(obj)

}
