package cz.encircled.eplayer

import cz.encircled.eplayer.core.SeriesFinder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * @author encir on 30-Aug-20.
 */
class SeriesFinderTest {

    private val seriesFinder: SeriesFinder = SeriesFinder()

    @Test
    fun testIsSeries() {
        assertTrue(seriesFinder.isSeries("Better.Call.Saul.S05E01.SomeAdditionalText"))
        assertTrue(seriesFinder.isSeries("Better.Call.Saul.S12E02.AnotherAdditionalText"))
        assertFalse(seriesFinder.isSeries("Better.Call.Saul.S1E.AnotherAdditionalText"))
        assertFalse(seriesFinder.isSeries("Better.Call.Saul.SE2.AnotherAdditionalText"))
        assertFalse(seriesFinder.isSeries("Better.Call.Saul.SE.AnotherAdditionalText"))
    }

    @Test
    fun testSeriesName() {
        assertEquals("Better.Call.Saul.", seriesFinder.seriesName("Better.Call.Saul.S05E01.SomeAdditionalText"))
        assertEquals("Better.Call.Saul.", seriesFinder.seriesName("Better.Call.Saul.S12E02.AnotherAdditionalText"))
    }

}