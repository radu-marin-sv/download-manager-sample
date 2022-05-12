package com.softvision.downloadmanagersample

import android.app.DownloadManager
import android.net.Uri

data class Document(
    val uri: Uri,
    val title: String,
    val status: String,
    val internalStatus: Int = -1,
    val localUri: Uri? = null
)