package com.dan.timelapse.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract


class UriFile(
    val context: Context,
    val uri: Uri,
    val name: String,
    val mime: String,
    private val documentId: String
) {

    companion object {
        private val DOCUMENT_ID_COLUMNS = listOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            "_id"
        )

        private fun getColumnIndex( cursor: Cursor, columnNames: List<String> ): Int {
            for (columnName in columnNames) {
                val index = cursor.getColumnIndex(columnName)
                if (index >= 0) return index
            }
            return -1
        }

        private fun queryUri( context: Context, uriQuery: Uri, uri: Uri, onlyFirstRecord: Boolean, isTreeUri: Boolean ): List<UriFile> {
            val result = mutableListOf<UriFile>()
            var cursor: Cursor? = null

            try {
                cursor = context.contentResolver.query(uriQuery, null, null, null, null)
                if (null != cursor) {
                    val indexDocumentId = getColumnIndex(cursor, DOCUMENT_ID_COLUMNS)
                    val indexDisplayName = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    val indexMime = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)

                    if (indexDocumentId >= 0 && indexDisplayName >= 0 && indexMime >= 0) {
                        while (cursor.moveToNext()) {
                            val documentId = cursor.getString(indexDocumentId)
                            val mime = cursor.getString(indexMime)

                            result.add(
                                UriFile(
                                    context,
                                    if (isTreeUri) DocumentsContract.buildDocumentUriUsingTree(uri, documentId) else uri,
                                    cursor.getString(indexDisplayName),
                                    mime,
                                    documentId
                                )
                            )

                            if (onlyFirstRecord) break
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if( null != cursor) {
                try {
                    cursor.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            return result.toList()
        }

        private fun fromTreeDocumentId( context: Context, treeUri: Uri, documentId: String ): UriFile? {
            val uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
            val list = queryUri(context, uri, uri, onlyFirstRecord = true, isTreeUri = true)
            return if (list.isNotEmpty()) list[0] else null
        }

        fun fromTreeUri(context: Context, uri: Uri): UriFile? {
            try {
                val documentId = if (DocumentsContract.isDocumentUri(context, uri)) {
                    DocumentsContract.getDocumentId(uri)
                } else {
                    DocumentsContract.getTreeDocumentId(uri)
                }

                return fromTreeDocumentId(context, uri, documentId)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }

        fun fromSingleUri(context: Context, uri: Uri): UriFile? {
            try {
                val list = queryUri(context, uri, uri, onlyFirstRecord = true, isTreeUri = false)
                return if (list.isNotEmpty()) list[0] else null
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }
    }

    val isDirectory = DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)
    val isPhoto = mime.startsWith("image/")
    val isVideo = mime.startsWith("video/")

    fun listFiles(): List<UriFile> {
        try {
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, documentId) ?: return listOf<UriFile>()
            return queryUri(context, childrenUri, uri, onlyFirstRecord = false, isTreeUri = true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return listOf()
    }
}