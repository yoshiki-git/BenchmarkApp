package com.example.benchmarkapp

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.StatFs
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.util.Log
import java.io.File
import java.util.*

class MyService : Service() {

    val TAG="TestApp"

    private lateinit var context: Context
    private lateinit var getLogData: GetLogData
    private lateinit var getRamInfo: GetRamInfo
    private lateinit var file: File

    private val getTimeData=GetTimeData()


    override fun onCreate() {
        super.onCreate()
        //context取得
        context=applicationContext
        //getLogData生成
        getLogData= GetLogData(context)
        getRamInfo = GetRamInfo(context)
        //ファイル名を現在時刻に設定する
        val start_time=getTimeData.getFileName()
        //拡張子をつける
        val fileName=start_time+"_Log"+".txt"
        file=getLogData.getFileStatus(fileName)


        val columns= arrayOf("CPU","RAM","ROM","fps")
        getLogData.getColumn(file,columns)

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val requestCode = intent!!.getIntExtra("REQUEST_CODE", 0)
        //    val context = applicationContext
        val channelId = "default"
        val title = context.getString(R.string.app_name)

        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager


        // Notification　Channel 設定
        val channel = NotificationChannel(
            channelId, title, NotificationManager.IMPORTANCE_DEFAULT
        )

        notificationManager.createNotificationChannel(channel)
        val notification = Notification.Builder(context, channelId)
            .setContentTitle(title) // android標準アイコンから
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentText("Monitoring Now")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setWhen(System.currentTimeMillis())
            .build()

          // startForeground 第一引数のidで通知を識別
             startForeground(9999, notification)

              getRomUsage()
              getRamInfo.update_property()
              Log.d(TAG,"テストだよ:${getRamInfo.ramUsage}%")


           //return START_NOT_STICKY;
           //return START_STICKY;
             return START_REDELIVER_INTENT
    }




        @SuppressLint("DiscouragedPrivateApi")
        private fun getRomUsage(){

            val storageManager = context.getSystemService(StorageManager::class.java)

            //システムストレージの取得
            val statFs_system = StatFs("/system")
            // 総容量
            val totalSpase1: Long = statFs_system.blockCountLong * statFs_system.blockSizeLong / 1024L / 1024L
            // 空き容量
            val freeSpase1: Long = statFs_system.availableBlocksLong * statFs_system.blockSizeLong / 1024L / 1024L
            // 使用容量
            val usedSpase1: Long = totalSpase1 - freeSpase1
            Log.d(TAG,"Label:システムストレージ")
            Log.d(TAG, " Used space: ${String.format(Locale.US, "%,12d", usedSpase1)}MB")
            Log.d(TAG, " Free space: ${String.format(Locale.US, "%,12d", freeSpase1)}MB")
            Log.d(TAG, "Total space: ${String.format(Locale.US, "%,12d", totalSpase1)}MB")


            for (storageVolume: StorageVolume in storageManager.storageVolumes) {
                // ストレージボリュームのリストを取得し、1件づつ処理
                // ストレージボリュームから絶対パスを取得
                val path: String = when {
                    Build.VERSION.SDK_INT>= Build.VERSION_CODES.R -> {
                        // Android 11以降
                        storageVolume.directory?.absolutePath
                    }
                    else -> {
                        // Android 10以前
                        // NOTE: Android10以前ではgetPathがprivateなため無理やり実行して取得
                        val getPath = StorageVolume::class.java.getDeclaredMethod("getPath")
                        getPath.invoke(storageVolume) as String?
                    }
                } ?: continue // 絶対パスが取得できない場合は、スキップ

                val label: String = when {
                    Build.VERSION.SDK_INT>= Build.VERSION_CODES.R -> {
                        // Android 11以降
                        storageVolume.getDescription(context)
                    }
                    else -> {
                        storageVolume.getDescription(context)
                    }
                } ?: continue // 絶対パスが取得できない場合は、スキップ

                val statFs = StatFs(path)
                // 総容量
                val totalSpase: Long = statFs.blockCountLong * statFs.blockSizeLong / 1024L / 1024L
                // 空き容量
                val freeSpase: Long = statFs.availableBlocksLong * statFs.blockSizeLong / 1024L / 1024L
                // 使用容量
                val usedSpase: Long = totalSpase - freeSpase

                Log.d(TAG, "Path: $path")
                Log.d(TAG,"Label: $label")
                Log.d(TAG, " Used space: ${String.format(Locale.US, "%,12d", usedSpase)}MB")
                Log.d(TAG, " Free space: ${String.format(Locale.US, "%,12d", freeSpase)}MB")
                Log.d(TAG, "Total space: ${String.format(Locale.US, "%,12d", totalSpase)}MB")
            }

        }


         override fun onBind(intent: Intent): IBinder {
           TODO("Return the communication channel to the service.")
         }
}