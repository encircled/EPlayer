package cz.encircled.eplayer.view.controller

import cz.encircled.eplayer.model.SingleMedia
import cz.encircled.eplayer.view.UiDataModel
import cz.encircled.eplayer.view.UiFolderMedia
import cz.encircled.eplayer.view.UiPlayableMedia
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import kotlin.test.Test
import kotlin.test.assertEquals

class TestGroupByFolderMediaProcessor {

    @Test
    fun `test all on root level`() {
        val processor = getProcessor()

        val result = processor.process(
            listOf(
                "C:\\test.mkv".media(),
                "C:\\some".folder(),
            ).asSequence()
        ).toList()

        assertEquals(listOf("C:\\test.mkv".media()), result)
    }

    @Test
    fun `test nested playable`() {
        val processor = getProcessor()

        val result = processor.process(
            listOf(
                "C:\\some2\\test.mkv".media(),
                "C:\\some2\\test2.mkv".media(),
                "C:\\some\\test.mkv".media(),
                "C:\\some".folder(),
            ).asSequence()
        ).toList()

        assertEquals(listOf("C:\\some2".folder(), "C:\\some".folder()), result)
        assertEquals(2, (result[0] as UiFolderMedia).nestedMedia.size)
        assertEquals(1, (result[1] as UiFolderMedia).nestedMedia.size)
    }

    @Test
    fun `test nested playable and folder`() {
        val processor = getProcessor()

        val result = processor.process(
            listOf(
                "C:\\some2\\test.mkv".media(),
                "C:\\some\\test.mkv".media(),
                "C:\\some".folder(),
                "C:\\some3\\1\\some3".folder(),
                "C:\\some3\\1\\some3\\2.mkv".media(),
                "C:\\some3\\1\\some4\\1.mkv".media(),
                "C:\\some4\\1\\some4".folder(),
            ).asSequence()
        ).toList()

        assertEquals(listOf("C:\\some2".folder(), "C:\\some".folder(), "C:\\some3".folder()), result)
        assertEquals(1, (result[0] as UiFolderMedia).nestedMedia.size)
        assertEquals(1, (result[1] as UiFolderMedia).nestedMedia.size)
        assertEquals(2, (result[2] as UiFolderMedia).nestedMedia.size)
    }

    private fun getProcessor() =
        GroupByFolderMediaProcessor(
            UiDataModel(
                selectedTab = SimpleStringProperty("C:"),
                flatView = SimpleBooleanProperty(false),
                selectedFullPath = SimpleStringProperty("C:")
            )
        )

    private fun String.media(): UiPlayableMedia = UiPlayableMedia(SingleMedia(this))

    private fun String.folder(): UiFolderMedia = UiFolderMedia(this.split("\\")[1], this, listOf())

}