package com.example.bluetoothapp.infrastructure

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter

class MeasurementFileRepository(
    private val applicationContext: Context
) {
    fun exportCsvToDownloads(fileName: String, csvContent: String) : Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                put(
                    MediaStore.Downloads.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS
                )
            }

            val contentResolver = applicationContext.contentResolver

            //Log.d("MeasurementFileRepository", "ContentValues: " + contentValues.toString())
            
            try {
                val uri =
                    contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                        ?: return false

                //Log.d("MeasurementFileRepository", "Uri: " + uri)

                val outputStream = contentResolver.openOutputStream(uri) ?: return false

                outputStream.use { output ->
                    BufferedWriter(OutputStreamWriter(output)).use { writer ->
                        writer.write(csvContent)
                        writer.flush()
                    }
                    //Log.d("MeasurementFileRepository", "Succeeded exporting")
                }
            }
            catch(exception : Exception) {
                Log.e("MeasurementFileRepository", "Exception occurred", exception)
                return false
            }
        } else {
            try {
                val downloadsDirectory =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDirectory, "$fileName.csv")
                file.writeText(csvContent)
            }
            catch(exception : Exception) {
                Log.e("MeasurementFileRepository", "Exception occurred", exception)
                return false;
            }
        }
        return true
    }
}