package cz.encircled.eplayer.service

import cz.encircled.eplayer.model.PlayableMedia

/**
 * @author Encircled on 11/06/2014.
 */
interface FolderScanService {

    fun getMediaInFolder(path: String, callback: CancelableExecution<List<PlayableMedia>>)

}