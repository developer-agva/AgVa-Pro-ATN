package com.agvahealthcare.ventilator_ext

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.DownloadManager.Query
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.currentDownloadProgress
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.downloadId
import com.agvahealthcare.ventilator_ext.utility.FILE_DESTINATION
import com.agvahealthcare.ventilator_ext.utility.FILE_IS_AUTO_UPDATE
import com.agvahealthcare.ventilator_ext.utility.utils.IntentFactory
import java.io.File

const val TAG = "OTA TESTING"

class DownloadController(private val context: Context, private val url: String,private val isAuto: Boolean) {

    companion object {
        private const val FILE_NAME = "app_release.apk"
        private const val FILE_BASE_PATH = "file://"
        private const val MIME_TYPE = "application/vnd.android.package-archive"
        private const val PROVIDER_PATH = ".provider"
    }

    private var downloadManager: DownloadManager? = null
    private var onComplete: BroadcastReceiver? = null

    fun removeDownload(){
        downloadManager?.remove(downloadId)
    }

    fun enqueueDownload() {

        var destination = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
        destination += FILE_NAME

        val uri = Uri.parse("$FILE_BASE_PATH$destination")

        val file = File(destination)
        if (file.exists() && isAuto){
            Log.i("testing_ota","file exists")
            sendBroadCastForInstall(destination)
        }else{
            Log.i("testing_ota","file not exists")
            if (downloadId == 0L){
                file.delete()
                downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val downloadUri = Uri.parse(url)
                val request = DownloadManager.Request(downloadUri).apply {
                    setMimeType(MIME_TYPE)
                    setTitle("app_release.apk")
                    allowScanningByMediaScanner()
                    setDescription(context.getString(R.string.downloading))
                }

                // set destination
                request.setDestinationUri(uri)
                Log.i("testing_ota", uri.toString())

                showInstallOption(destination)
                // Enqueue a new download and same the referenceId

                downloadId = downloadManager?.enqueue(request)!!
                monitorDownloadProgress(downloadManager)
            }
        }
    }

    @SuppressLint("Range")
    private fun monitorDownloadProgress(downloadManager: DownloadManager?) {
        val query = Query().setFilterById(downloadId)

        // Run a thread to periodically check the download progress
        Thread {
            var downloading = true
            while (downloading) {
                val cursor = downloadManager?.query(query)
                if (cursor?.moveToFirst()!!) {
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    val bytesDownloaded = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val totalBytes = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                    if (status == DownloadManager.STATUS_RUNNING) {
                        val progress = (bytesDownloaded * 100 / totalBytes).toInt()
                        currentDownloadProgress = progress
                        Log.d(TAG, "monitorDownloadProgress: $progress")

                    } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false

                    } else if (status == DownloadManager.STATUS_FAILED) {
                        downloading = false

                    }
                }
                cursor.close()
                Thread.sleep(1000) // Sleep for 1 second before checking again
            }
        }.start()
    }

    private fun showInstallOption(
        destination: String,
    ) {

        // set BroadcastReceiver to install app when .apk is downloaded
        onComplete = object : BroadcastReceiver() {
            @SuppressLint("Range")
            override fun onReceive(
                context: Context,
                intent: Intent
            ) {

                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (downloadId == id) {

                    Log.i("testing_ota","download id matched")
                    val action = intent.action
                    if (action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                        val query = Query()
                        query.setFilterById(
                            downloadId
                        )

                        val cursor = downloadManager?.query(query)!!
                        if (cursor.moveToFirst()) {

                            // if download completed...
                            if (cursor.count > 0) {
                                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                                Log.i("testing_ota","download status : $status")


                                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                    // So something here on success
                                    Log.i("testing_ota","download success")
                                    sendBroadCastForInstall(destination)
                                    downloadId = 0L
                                } else {
                                     downloadManager?.remove(downloadId)
                                    downloadId = 0L
                                    Log.i("testing_ota","download failed")
                                    // So something here on failed.
                                }
                            }

                        } else {
                            // if download cancelled..
                            downloadManager?.remove(downloadId)
                            downloadId = 0L
                        }
                    }

                } else {
                    downloadManager?.remove(downloadId)
                    downloadId = 0L
                }
            }
        }
        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    private fun sendBroadCastForInstall(destination: String){
        Log.i("testing_ota","in send broadcast $destination")
        val i = Intent(IntentFactory.ACTION_DOWNLOADED_ALREADY_LETS_INSTALL)
        i.putExtra(FILE_DESTINATION, destination)
        i.putExtra(FILE_IS_AUTO_UPDATE, isAuto)
        context.sendBroadcast(i)
    }

    fun installApk(destination:String){

        val contentUri = FileProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + PROVIDER_PATH,
            File(destination)
        )
        Log.i("testing_ota","in send installation $contentUri")
        val install = Intent(Intent.ACTION_VIEW)
        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        install.data = contentUri
        context.startActivity(install)
        onComplete?.let {
            context.unregisterReceiver(it)
        }
    }

}

