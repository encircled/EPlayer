package cz.encircled.eplayer.service

import cz.encircled.eplayer.model.GenericTrackDescription
import kotlin.test.Test
import kotlin.test.assertEquals

class MediaSettingsSuggestionsTest {

    private val suggestions: MediaSettingsSuggestions = MediaSettingsSuggestionsImpl()

    @Test
    fun testSuggestDisabledSubtitlesWhenHasNoForced() {
        assertEquals(
            3,
            suggestions.suggestSubtitle(
                "cz", true, listOf(
                    GenericTrackDescription(1, "Some [cz] info"),
                    GenericTrackDescription(2, "Some [en] info"),
                    GenericTrackDescription(3, "Disabled"),
                    GenericTrackDescription(4, "Some [de] Forced"),
                )
            ).track.id
        )
    }

    @Test
    fun testSuggestForcedSubtitles() {
        assertEquals(
            4,
            suggestions.suggestSubtitle(
                "cz", true, listOf(
                    GenericTrackDescription(1, "Some [cz] info"),
                    GenericTrackDescription(2, "Some [de] info"),
                    GenericTrackDescription(3, "Disabled"),
                    GenericTrackDescription(4, "Some [cz] Forced"),
                )
            ).track.id
        )
    }

    @Test
    fun testSuggestPreferredLanguageSubtitles() {
        assertEquals(
            3,
            suggestions.suggestSubtitle(
                "de", false, listOf(
                    GenericTrackDescription(1, "Some [cz] info"),
                    GenericTrackDescription(2, "Some [de] Forced"),
                    GenericTrackDescription(3, "Some [de] info"),
                    GenericTrackDescription(4, "Some [ru] info"),
                )
            ).track.id
        )
    }

    @Test
    fun testSuggestFirstSubtitles() {
        assertEquals(
            1,
            suggestions.suggestSubtitle(
                "cz", false, listOf(
                    GenericTrackDescription(1, ""),
                    GenericTrackDescription(1, "Some [de] info"),
                    GenericTrackDescription(1, "Some [ru] Forced"),
                    GenericTrackDescription(1, "Disabled"),
                )
            ).track.id
        )

        assertEquals(
            1,
            suggestions.suggestSubtitle(
                "cz", false, listOf(
                    GenericTrackDescription(1, "Some [de] info"),
                    GenericTrackDescription(2, "Some [cz] Forced"),
                    GenericTrackDescription(3, "Some [en] info"),
                )
            ).track.id
        )
    }

    @Test
    fun testSuggestPreferredLanguageAudio() {
        assertEquals(
            2,
            suggestions.suggestAudioTrack(
                "de", listOf(
                    GenericTrackDescription(1, "Some [cz] info"),
                    GenericTrackDescription(2, "Some [de] 1"),
                    GenericTrackDescription(3, "Some [de] 2"),
                    GenericTrackDescription(4, "Some [ru] info"),
                )
            ).track.id
        )
    }

    @Test
    fun testSuggestFirstLanguageAudio() {
        assertEquals(
            1,
            suggestions.suggestAudioTrack(
                "de", listOf(
                    GenericTrackDescription(1, "Some [cz] info"),
                    GenericTrackDescription(2, "Some [ru] 1"),
                    GenericTrackDescription(3, "Some [en] 2"),
                    GenericTrackDescription(4, "Some [ru] info"),
                )
            ).track.id
        )
    }

}