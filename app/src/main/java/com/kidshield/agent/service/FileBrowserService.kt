package com.kidshield.agent.service

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileBrowserService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    data class FileItem(
        val name: String,
        val path: String,
        val size: Long,
        val modified: Long,
        val type: String
    )

    suspend fun getImages(): List<FileItem> = withContext(Dispatchers.IO) {
        scanMediaStore(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image")
    }

    suspend fun getVideos(): List<FileItem> = withContext(Dispatchers.IO) {
        scanMediaStore(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video")
    }

    suspend fun getAudio(): List<FileItem> = withContext(Dispatchers.IO) {
        scanMediaStore(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "audio")
    }

    suspend fun getDownloads(): List<FileItem> = withContext(Dispatchers.IO) {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        scanDirectory(downloadsDir, "download")
    }

    suspend fun getDocuments(): List<FileItem> = withContext(Dispatchers.IO) {
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        scanDirectory(documentsDir, "document")
    }

    private fun scanMediaStore(uri: Uri, type: String): List<FileItem> {
        val items = mutableListOf<FileItem>()
        val projection = arrayOf(
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_MODIFIED
        )
        context.contentResolver.query(uri, projection, null, null, "${MediaStore.MediaColumns.DATE_MODIFIED} DESC")?.use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val dateIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
            while (cursor.moveToNext()) {
                items.add(FileItem(
                    name = cursor.getString(nameIndex),
                    path = cursor.getString(pathIndex),
                    size = cursor.getLong(sizeIndex),
                    modified = cursor.getLong(dateIndex) * 1000,
                    type = type
                ))
            }
        }
        return items
    }

    private fun scanDirectory(dir: File, type: String): List<FileItem> {
        val items = mutableListOf<FileItem>()
        dir.listFiles()?.forEach { file ->
            if (file.isFile) {
                items.add(FileItem(
                    name = file.name,
                    path = file.absolutePath,
                    size = file.length(),
                    modified = file.lastModified(),
                    type = type
                ))
            }
        }
        return items.sortedByDescending { it.modified }
    }

    fun deleteFile(path: String): Boolean {
        return File(path).delete()
    }

    fun readFileBytes(path: String): ByteArray? {
        return try {
            File(path).readBytes()
        } catch (e: Exception) {
            null
        }
    }
}
