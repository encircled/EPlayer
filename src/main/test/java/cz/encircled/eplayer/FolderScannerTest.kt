package cz.encircled.eplayer

import cz.encircled.eplayer.core.OnDemandFolderScanner
import cz.encircled.eplayer.model.MediaSeries
import cz.encircled.eplayer.service.CancelableExecution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FolderScannerTest : BaseTest() {

    private val folderScanner: OnDemandFolderScanner = OnDemandFolderScanner(core)

    @Test
    fun testSeriesCreatedAndReused() {
        val initialSize = core.cacheService.getCachedMedia().size

        repeat(3) {
            folderScanner.getMediaInFolder(pathToResources.resolve("video/series").path, CancelableExecution {
                assertEquals(1, it.size)
                assertTrue(it[0] is MediaSeries)
                assertEquals(2, (it[0] as MediaSeries).series.size)
            })
        }

        assertEquals(initialSize + 1, core.cacheService.getCachedMedia().size)
    }

}