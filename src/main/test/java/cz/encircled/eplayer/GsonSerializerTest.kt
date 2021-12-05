package cz.encircled.eplayer

import cz.encircled.eplayer.model.*
import cz.encircled.eplayer.util.MediaWrapper
import cz.encircled.fswing.observable.observableList
import cz.encircled.fswing.serialization.GsonSerializer
import cz.encircled.fswing.serialization.Serializer
import javafx.beans.property.SimpleLongProperty
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class GsonSerializerTest {

    private val gson: Serializer = GsonSerializer()
    private val random = Random(1000)

    @Test
    fun testLongProperty() {
        val original = SimpleLongProperty(123)
        val json = gson.fromObject(original)
        assertEquals("123", json)
        assertEquals(original.value, gson.toObject(json, SimpleLongProperty::class.java).value)
    }

    @Test
    fun testReadWriteMediaSingleMedia() {
        val single = getSingleMedia()
        assertMediaEquals(single, gson.toObject(gson.fromObject(single), SingleMedia::class.java))

        val fromJson = gson.toObject(gson.fromObject(SingleMedia("E:/")), SingleMedia::class.java)
        assertMediaEquals(SingleMedia("E:/"), fromJson)
    }

    @Test
    fun testReadWriteMediaMediaSeries() {
        val single = MediaSeries("Some", "E:/Some", arrayListOf(getSingleMedia()))

        val right = gson.toObject(gson.fromObject(single), MediaSeries::class.java)
        assertMediaEquals(single, right)
    }

    @Test
    fun testMediaWrapper() {
        val element = getSingleMedia()
        assertEquals(
            MediaWrapper(),
            gson.toObject(gson.fromObject(MediaWrapper()), MediaWrapper::class.java)
        )
        assertEquals(
            MediaWrapper(listOf(element)),
            gson.toObject(gson.fromObject(MediaWrapper(listOf(element))), MediaWrapper::class.java)
        )
    }

    @Test
    fun testSetting() {
        val expected = AppSettings("ru", "E:/", false, 100, 50, mutableListOf("E:/", "C:/"))
        assertEquals(expected, gson.toObject(gson.fromObject(expected), AppSettings::class.java))
    }

    fun assertMediaEquals(left: PlayableMedia, right: PlayableMedia) {
        assertEquals(left, right)

        assertEquals(left.duration.get(), right.duration.get())
        assertEquals(left.time.get(), right.time.get())
        assertEquals(left.path, right.path)
        assertEquals(left.watchDate, right.watchDate)

        if (left is MediaSeries && right is MediaSeries) {
            left.series.forEachIndexed { index, leftEntry -> assertMediaEquals(leftEntry, right.series[index]) }
        } else if (left is SingleMedia && right is SingleMedia) {
            assertEquals(left.mediaFile().path, right.mediaFile().path)
            assertEquals(left.preferredSubtitle, right.preferredSubtitle)
            assertEquals(left.preferredAudio, right.preferredAudio)
            assertEquals(left.metaCreationDate, right.metaCreationDate)
            assertEquals(left.bookmarks, right.bookmarks)
        }
    }

    private fun getSingleMedia(): SingleMedia {
        return SingleMedia(
            "E:/",
            time = SimpleLongProperty(random.nextLong()),
            duration = SimpleLongProperty(random.nextLong()),
            watchDate = System.currentTimeMillis() - random.nextLong(),
            preferredAudio = GenericTrackDescription(2, "2 desc"),
            preferredSubtitle = GenericTrackDescription(3, "3 desc"),
            metaCreationDate = "2020-01-01",
            bookmarks = observableList(MediaBookmark(1), MediaBookmark(2))
        )
    }


}