package com.example.mycallapp.commons.utils

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Writer
import java.nio.charset.StandardCharsets
import java.util.Date

class Utils {
    companion object {
        private val TAG: String = Utils::class.java.simpleName

        private fun logtoFile(
            context: Context,
            fileUri: String,
            mytext: String,
            fileMode: Int
        ): Boolean {
            val yourmilliseconds = System.currentTimeMillis()
            val sdf = SimpleDateFormat("MMM dd,yyyy HH:mm")
            val resultdate = Date(yourmilliseconds)
            try {
                val fos = context.openFileOutput(fileUri, fileMode)
                val out: Writer = OutputStreamWriter(fos)
                out.write(sdf.format(resultdate) + ": Start:" + mytext + "End")
                out.close()
                return true
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
        }

        fun load(context: Context, fileUri: String): String? {
            var contents = ""
            try {
                val fis = context.openFileInput(fileUri)
                val inputStreamReader =
                    InputStreamReader(fis, StandardCharsets.UTF_8)
                val stringBuilder = StringBuilder()
                try {
                    BufferedReader(inputStreamReader).use { reader ->
                        var line = reader.readLine()
                        while (line != null) {
                            stringBuilder.append(line).append('\n')
                            line = reader.readLine()
                        }
                    }
                } catch (e: IOException) {
                    Log.d("SARA ", "Utils. load BufferedReader IOException e:" + e.message)
                } finally {
                    contents = stringBuilder.toString()
                }
                inputStreamReader.close()
                return contents
            } catch (e: IOException) {
                Log.d("SARA ", "Utils. load  General IOException e:" + e.message)
                return null
            }
        }

        fun submitLogs(context: Context, fileUri: String) {
            val logText = load(context, fileUri)
        }

        fun clearLogs(context: Context, fileUri: String) {
            Log.d("SARA", " fileUri ::: <<<<<<<<<<<" + fileUri)
            if (context.deleteFile(fileUri)) {
                Log.d("SARA", " AppLogFile file deleted <<<<<<<<<<<")
            } else {
                Log.d("SARA", "AppLogFile Error in  delete file <<<<<<<<<<<")
            }
            if (!fileUri.isEmpty()) {
                try {
                    val file = File(fileUri)
                    if (file.exists()) file.delete()
                    Log.d("SARA", "fileUri deleting file ")
                } catch (e: Exception) {
                    Log.e("SARA", "Exception while deleting file " + e.message)
                }
            }
        }

        fun saveLogs(context: Context, mytext: String, fileUri: String) {
            logtoFile(context, mytext, fileUri, Context.MODE_PRIVATE or Context.MODE_APPEND)
        }

        fun writeFileExternalStorage(context: Context,fileUri: String, logtext: String) {
            try {
                val yourmilliseconds = System.currentTimeMillis()
                val sdf = SimpleDateFormat("MMM dd,yyyy HH:mm")
                val resultdate = Date(yourmilliseconds)
                var outPut = sdf.format(resultdate) + ": Start: " + logtext + " :End \n"
                context.contentResolver.openFileDescriptor(fileUri.toUri(), "wa")?.use {
                    FileOutputStream(it.fileDescriptor).use {
                        it.write(outPut.toByteArray())
                    }
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

   }
}