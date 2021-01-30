package cz.encircled.eplayer

import cz.encircled.eplayer.model.MediaSeries
import cz.encircled.eplayer.service.JsonCacheService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JsonCacheServiceTest : BaseTest() {

    @Test
    fun testAddSameSeriesEpisode() {
        val service = JsonCacheService(core)

        val episode1 = pathToResources.resolve("video/Series/Series.s1e1.mkv").path

        service.createIfAbsent(episode1)
        service.createIfAbsent(episode1)

        val series = service.getCachedMedia().filter {
            it is MediaSeries && it.name == "Series."
        }
        assertTrue(series.size == 1 && series[0] is MediaSeries)
        assertTrue((series[0] as MediaSeries).series.size == 2)

        // Verify that duplicates are not instantiated and not being queued for the metadata
        assertEquals(2, (core.metaInfoService as TestMetadataService).added.size)
    }

}