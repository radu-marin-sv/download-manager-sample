package com.softvision.downloadmanagersample

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class DocumentViewModel(application: Application) : AndroidViewModel(application) {
    // This can come from whenever is needed (network, database, manual entry, etc)
    private val documentFlow: Flow<List<Document>> = flowOf(
        listOf(
            Document(
                uri = Uri.parse("https://www.learningcontainer.com/download/sample-pdf-download-10-mb/#"),
                title = "sample_10mb.pdf"
            ),
            Document(
                uri = Uri.parse("https://www.learningcontainer.com/download/sample-pdf-with-images/#"),
                title = "sample_with_pics.pdf"
            ),
        )
    )

    val documents: LiveData<List<Document>> = documentFlow.asLiveData()
}