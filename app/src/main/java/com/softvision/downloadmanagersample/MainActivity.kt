package com.softvision.downloadmanagersample

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {
    private val viewModel: DocumentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = DocumentAdapter {
            viewModel.onDocumentTap(it)
        }
        files.adapter = adapter

        viewModel.openDocument.observe(this) {
            if (it != null) {
                val file = it.toFile()
                val fileUri: Uri? = try {
                    FileProvider.getUriForFile(
                        this@MainActivity,
                        "com.softvision.downloadmanagersample.fileprovider",
                        file)
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                    null
                }

                if (fileUri != null) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(fileUri, "application/pdf")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION )
                    startActivity(intent)
                }

                viewModel.onDocumentOpened()
            }
        }

        viewModel.documents.observe(this) {
            adapter.submitList(it)
        }
    }
}