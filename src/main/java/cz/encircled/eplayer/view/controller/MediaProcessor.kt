package cz.encircled.eplayer.view.controller

import cz.encircled.eplayer.model.SingleMedia
import cz.encircled.eplayer.view.*
import cz.encircled.eplayer.view.AggregationType.*
import java.io.File
import java.time.LocalDate
import java.util.regex.Pattern

interface MediaProcessor {

    fun process(media: Sequence<UiMedia>): Sequence<UiMedia>

}

class NameFilterMediaProcessor(val dataModel: UiDataModel) : MediaProcessor {

    override fun process(media: Sequence<UiMedia>): Sequence<UiMedia> {
        val filter = dataModel.filter.get()

        return if (filter.isNotBlank()) {
            val p = Pattern.compile("(?i).*" + filter.replace(" ".toRegex(), ".*") + ".*")
            val m = p.matcher("")
            return media.filter { m.reset(it.name()).matches() }
        } else media
    }

}

class SortingMediaProcessor(val dataModel: UiDataModel) : MediaProcessor {

    private val typeToComparator: Map<SortType, Comparator<UiMedia>> = mapOf(
        SortType.Duration to Comparator { o1, o2 ->
            if (o1 is UiPlayableMedia && o2 is UiPlayableMedia) {
                o1.media.duration.value.compareTo(o2.media.duration.value)
            } else if (o1 is UiPlayableMedia) 1 else -1
        },
        SortType.Name to Comparator { o1, o2 ->
            o1.name().compareTo(o2.name())
        },
        SortType.Date to Comparator { o1, o2 ->
            if (o1 is UiPlayableMedia && o2 is UiPlayableMedia) {
                o1.media.watchDate.compareTo(o2.media.watchDate)
            } else if (o1 is UiPlayableMedia) 1 else -1
        }
    )

    override fun process(media: Sequence<UiMedia>): Sequence<UiMedia> {
        val c = typeToComparator.getValue(dataModel.sortType.get())

        return media.sortedWith { o1, o2 ->
            if (o1 is UiFolderMedia && o2 !is UiFolderMedia) -1
            else if (o2 is UiFolderMedia && o1 !is UiFolderMedia) 1
            else c.compare(o1, o2)
        }
    }
}

class GroupByFolderMediaProcessor(val dataModel: UiDataModel) : MediaProcessor {

    override fun process(media: Sequence<UiMedia>): Sequence<UiMedia> {
        if (dataModel.flatView.get() || dataModel.aggregationType.get() != AggregationType.None ||
            dataModel.selectedTab.get().equals(QUICK_NAVI)
        ) return media

        val root = getRoot()

        val folderToNestedMedia = media.filterIsInstance<UiPlayableMedia>().groupBy {
            val relativePath = it.media.path.substring(root.length)

            if (relativePath.contains(File.separator)) relativePath.split(File.separator)[0]
            else ""
        }

        val onRootLevel = folderToNestedMedia.getOrDefault("", listOf())
        val folders = folderToNestedMedia.filter { it.key.isNotEmpty() }
            .map {
                val children = it.value.map { m -> m.media }

                UiFolderMedia(it.key, root + it.key, children)
            }

        return (folders + onRootLevel).distinct().asSequence()
    }

    private fun getRoot(): String {
        val root = dataModel.selectedFullPath.get()
        return if (root.endsWith(File.separator)) root
        else root + File.separator
    }
}

class AggregationMediaProcessor(val dataModel: UiDataModel) : MediaProcessor {

    override fun process(media: Sequence<UiMedia>): Sequence<UiMedia> {
        // Skip aggregation, when dynamic filter is already applied (i.e. an aggregated folder is opened)
        val aggregationType = dataModel.aggregationType.get()
        if (dataModel.dynamicFilterForMedia.get() != null || aggregationType == AggregationType.None) {
            return media
        }

        return media
            .filterIsInstance<UiPlayableMedia>()
            .map { it.media }
            .filterIsInstance<SingleMedia>()
            .groupBy {
                val date = it.metaCreationDate.toDate()
                when (aggregationType) {
                    CreationYear -> date?.withDayOfYear(1)
                    CreationMonth -> date?.withDayOfMonth(1)
                    else -> date
                }
            }
            .map { (date, value) ->
                val name = buildNameForDate(date, aggregationType)

                UiFolderMedia(name, name, value) {
                    it is UiPlayableMedia && it.media is SingleMedia &&
                            sameDate(it.media.metaCreationDate.toDate(), date, aggregationType)
                }
            }
            .asSequence()
    }

    private fun buildNameForDate(date: LocalDate?, aggregationType: AggregationType) =
        if (date == null) "Not set" else when (aggregationType) {
            CreationYear -> "${date.year}"
            CreationMonth -> "${date.year}-${date.monthValue}"
            CreationDay -> "${date.year}-${date.monthValue}-${date.dayOfMonth}"
            else -> throw UnsupportedOperationException()
        }

}

class DynamicFilterMediaProcessor(val dataModel: UiDataModel) : MediaProcessor {
    override fun process(media: Sequence<UiMedia>): Sequence<UiMedia> {
        val dynamicFilter = dataModel.dynamicFilterForMedia.get()
        return if (dynamicFilter != null) {
            media.filter { dynamicFilter(it) }
        } else media
    }
}

private fun sameDate(left: LocalDate?, right: LocalDate?, aggregationType: AggregationType) =
    when {
        left == null || right == null -> left == right
        else -> left.year == right.year && (aggregationType == CreationYear || left.monthValue == right.monthValue) && (aggregationType != CreationDay || left.dayOfMonth == right.dayOfMonth)
    }

private fun String.toDate(): LocalDate? =
    if (isBlank()) null else LocalDate.parse(substring(0, 10))
