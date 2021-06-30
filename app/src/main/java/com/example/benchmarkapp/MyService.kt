package com.example.benchmarkapp

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.StatFs
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.util.Log
import android.view.Display
import android.view.WindowManager
import java.io.File
import java.lang.StringBuilder
import java.util.*

class MyService : Service() {

    val TAG="TestApp"

    private lateinit var context: Context
    private lateinit var getLogData: GetLogData
    private lateinit var getRamInfo: GetRamInfo
    private lateinit var file: File

    private lateinit var windowManager:WindowManager

    private val getCpuInfo = GetCpuInfo()
    private val getTimeData=GetTimeData()
    private var core_count:Int = 0



    override fun onCreate() {
        super.onCreate()
        //context取得
        context=applicationContext
        //getLogData生成
        getLogData= GetLogData(context)
        getRamInfo = GetRamInfo(context)

        //Windowマネージャー
        windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
        //ファイル名を現在時刻に設定する
        val start_time=getTimeData.getFileName()
        //拡張子をつける
        val fileName=start_time+"_Log"+".txt"
        file=getLogData.getFileStatus(fileName)

        //カラムに渡す配列の定義
        val columns = mutableListOf<String>()
        //CPUコア数の取得
        core_count = getCpuInfo.countCoreNum()
        for (i in 0..core_count){
            columns.add(i,"Core${i+1}")
        }
        //RAMのカラム追加
        columns.add("RAM")
        columns.add("fps")

  //      val columns= arrayOf("CPU","RAM","ROM","fps")
        //カラムをログに書き込む
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


              Log.d(TAG,"テストだよ:${getRamInfo.ramUsage}%")

            writeLog()


           //return START_NOT_STICKY;
           //return START_STICKY;
             return START_REDELIVER_INTENT
    }
        private fun writeLog(){
            // 定期取得処理は全部ここに入れる
            val mTimer = Timer(true)
            val mHandler= Handler()
            mTimer.schedule(object : TimerTask() {
                override fun run() {
                    mHandler.post(Runnable {

                        val stringBuilder=StringBuilder()
                        stringBuilder.append(getTimeData.getNowTime()).append(",")

                        //現在のCPU周波数を取得
                        val currentFreqs=getCpuInfo.takeCurrentCpuFreqs(core_count+1)
                        for (i in 0..core_count){
                            stringBuilder.append(currentFreqs[i]/1000).append(",")
                        }

                        //現在のRAM使用率を取得
                        getRamInfo.update_property()
                        stringBuilder.append(getRamInfo.usedmem.toInt()).append(",")

                        //リフレッシュレート
                        val rf =getRefresh()
                        stringBuilder.append(rf)

                        stringBuilder.append("\n")

                        getLogData.getLog(file,stringBuilder.toString())


                    })
                }
            }, 1, 1000) //1ミリ秒後にintervalミリ秒ごとの繰り返し
        }

    //リフレッシュレートの取得
        fun getRefresh():String{
            val display = windowManager.defaultDisplay
            val rf = display.refreshRate.toString()

            return rf
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