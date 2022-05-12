package com.softvision.downloadmanagersample

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import androidx.lifecycle.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DocumentViewModel(application: Application) : AndroidViewModel(application) {
    private val downloadManager = application.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    // This can come from whenever is needed (network, database, manual entry, etc)
    private val documentFlow: Flow<List<Document>> = flowOf(
        listOf(
            Document(
                uri = Uri.parse("https://dl.acm.org/doi/pdf/10.1145/329124.329126"),
                title = "The Computer for the 21st Century.pdf",
                status = ""
            ),
            Document(
                uri = Uri.parse("https://www.researchgate.net/profile/Ciprian-Dobre/publication/262293195_Reaching_for_the_clouds_contextually_enhancing_smartphones_for_energy_efficiency/links/53f740050cf2fceacc750e1b/Reaching-for-the-clouds-contextually-enhancing-smartphones-for-energy-efficiency.pdf"),
                title = "Reaching for the clouds: contextually enhancing smartphones for energy efficiency.pdf",
                status = ""
            ),
            Document(
                uri = Uri.parse("https://www.mdpi.com/1424-8220/22/7/2528/pdf"),
                title = "IoT and AI-Based Application for Automatic Interpretation of the Affective State of Children Diagnosed with Autism.pdf",
                status = ""
            ),
        )
    )

    val downloadsFlow = application.applicationContext.observeDownloads()

    val documents: LiveData<List<Document>> = documentFlow
        .combine(downloadsFlow) { documents, downloads ->
            mapDocuments(documents, downloads)
        }.asLiveData()

    private val _openDocument = MutableLiveData<Uri?>()
    val openDocument: LiveData<Uri?>
        get() = _openDocument

    fun onDocumentTap(document: Document) {
        when (document.internalStatus) {
            -1 -> downloadDocument(document)
            DownloadManager.STATUS_PAUSED -> {}
            DownloadManager.STATUS_PENDING -> {}
            DownloadManager.STATUS_FAILED -> downloadDocument(document)
            DownloadManager.STATUS_RUNNING -> {}
            DownloadManager.STATUS_SUCCESSFUL -> openDocument(document)
        }
    }

    fun onDocumentOpened() {
        _openDocument.value = null
    }

    private fun downloadDocument(document: Document) {
        downloadManager.enqueue(
            DownloadManager.Request(document.uri).apply {
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "papers/${document.title}")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE or DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            }
        )
    }

    private fun openDocument(document: Document) {
        _openDocument.value = document.localUri
    }

    private fun mapDocuments(documents: List<Document>, downloads: List<Download>): List<Document> {
        val uiDocs = documents.map { document ->
            val download = downloads.firstOrNull { it.uri == document.uri }
            if (download != null) {
                Document(
                    uri = document.uri,
                    title = document.title,
                    status = getStatusFromDownload(download),
                    internalStatus = download.status ?: -1,
                    localUri = download.localUri
                )
            } else {
                Document(
                    uri = document.uri,
                    title = document.title,
                    status = "Tap to download"
                )
            }
        }

        return uiDocs
    }

    private fun getStatusFromDownload(download: Download): String {
        return when {
            download.status == DownloadManager.STATUS_SUCCESSFUL -> "Tap to open"
            download.status == DownloadManager.STATUS_FAILED -> "Error: ${download.reason}"
            download.status == DownloadManager.STATUS_RUNNING -> "Downloaded: ${download.bytesSoFar} / ${download.totalBytes}"
            download.status == DownloadManager.STATUS_PENDING -> "Pending"
            download.status == DownloadManager.STATUS_PAUSED -> "Waiting for something: ${download.reason}"
            else -> "No idea what's going on"
        }
    }

    data class Download(
        val id: Long,
        val uri: Uri,
        val localUri: Uri?,
        val status: Int?,
        val reason: Int?,
        val bytesSoFar: Long?,
        val totalBytes: Long
    )

    private fun Context.observeDownloads(): Flow<List<Download>> {
        return callbackFlow {
            trySend(queryDownload())

            val thread = HandlerThread("downloads-thread")
            thread.start()

            val observer = object : ContentObserver(Handler(thread.looper)) {
                override fun onChange(selfChange: Boolean) {
                    if (!selfChange) {
                        trySend(queryDownload())
                    }
                }
            }

            contentResolver.registerContentObserver(MY_DOWNLOADS_URI, true, observer)

            awaitClose {
                contentResolver.unregisterContentObserver(observer)
                thread.looper.quitSafely()
            }
        }
    }

    companion object {
        private val MY_DOWNLOADS_URI = Uri.parse("content://downloads/my_downloads")
    }

    private fun queryDownload(): List<Download> {
        val downloads = mutableListOf<Download>()
        val cursor = downloadManager.query(DownloadManager.Query()) ?: return downloads

        with (cursor) {
            val id = getColumnIndex(DownloadManager.COLUMN_ID)
            val uri = getColumnIndex(DownloadManager.COLUMN_URI)
            val localUri = getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
            val status = getColumnIndex(DownloadManager.COLUMN_STATUS)
            val reason = getColumnIndex(DownloadManager.COLUMN_REASON)
            val bytesSoFar = getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val totalBytes = getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

            while (moveToNext()) {
                downloads.add(
                    Download(
                        id = getLong(id),
                        uri = Uri.parse(getString(uri)),
                        localUri = getString(localUri)?.let { Uri.parse(it) },
                        status = getInt(status),
                        reason = getInt(reason),
                        bytesSoFar = getLong(bytesSoFar),
                        totalBytes = getLong(totalBytes)
                    )
                )
            }
        }

        return downloads
    }
}