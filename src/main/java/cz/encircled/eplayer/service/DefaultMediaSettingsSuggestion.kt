package cz.encircled.eplayer.service

import cz.encircled.eplayer.model.GenericTrackDescription
import org.apache.logging.log4j.LogManager

data class MediaSettingsSuggestion(val track: GenericTrackDescription, val isPreferredLanguage: Boolean)

interface MediaSettingsSuggestions {

    /**
     * @param hasPrefLanguageAudioTrack - whether audio track with preferred language is available
     */
    fun suggestSubtitle(
        preferredLanguage: String,
        hasPrefLanguageAudioTrack: Boolean,
        available: List<GenericTrackDescription>,
    ): MediaSettingsSuggestion

    fun suggestAudioTrack(preferredLanguage: String, available: List<GenericTrackDescription>): MediaSettingsSuggestion

    fun suggestAudioPassThroughRequired(track: GenericTrackDescription): Boolean

}

class MediaSettingsSuggestionsImpl : MediaSettingsSuggestions {

    private val log = LogManager.getLogger()

    override fun suggestSubtitle(
        preferredLanguage: String,
        hasPrefLanguageAudioTrack: Boolean,
        available: List<GenericTrackDescription>
    ): MediaSettingsSuggestion {
        val suggestion = if (hasPrefLanguageAudioTrack) {
            available.firstMatch(
                { it.contains(preferredLanguage) && it.contains("force") },
                { it.contains("disable") }
            )
        } else {
            available.firstMatch(
                { it.contains(preferredLanguage) && !it.contains("force") }
            )
        }
        log.info("Suggested: ${suggestion.track.description}")
        return suggestion
    }

    override fun suggestAudioTrack(
        preferredLanguage: String,
        available: List<GenericTrackDescription>
    ): MediaSettingsSuggestion {
        val suggestion = available.firstMatch(
            { it.contains(preferredLanguage) }
        )
        log.info("Suggested: ${suggestion.track.description}")
        return suggestion
    }

    override fun suggestAudioPassThroughRequired(track: GenericTrackDescription): Boolean =
        track.description.contains("trhd")

    private fun List<GenericTrackDescription>.firstMatch(vararg predicates: (String) -> Boolean): MediaSettingsSuggestion {
        val descriptions = this
            .map { it.description.toLowerCase() }

        predicates.forEach {
            val index = descriptions.indexOfFirst(it)
            if (index >= 0) {
                return MediaSettingsSuggestion(this[index], true)
            }
        }

        return MediaSettingsSuggestion(this[0], false)
    }

}
