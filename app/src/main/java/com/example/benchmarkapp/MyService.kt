package com.example.benchmarkapp

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.io.File

class MyService : Service() {

    val TAG="TestApp"

    private lateinit var context: Context
    private lateinit var getLogData: GetLogData
    private var totalmem:Float?=null
    private lateinit var am:ActivityManager
    private lateinit var file: File

    private val getTimedata=GetTimeData()


    override fun onCreate() {
        super.onCreate()
        //context取得
        context=applicationContext
        //getLogData生成
        getLogData= GetLogData(context)
        //ファイル名を現在時刻に設定する
        val start_time=getTimedata.getFileName()
        //拡張子をつける
        val fileName=start_time+"_Log"+".txt"
        file=getLogData.getFileStatus(fileName)

        //ActivityManager取得
        am=getSystemService(ACTIVITY_SERVICE)as ActivityManager
        //メモリの最大値を取得している
        val meminfo=ActivityManager.MemoryInfo()
        am.getMemoryInfo(meminfo)
        totalmem=meminfo.totalMem.toFloat()
        Log.d(TAG,"TotalMemory:"+totalmem.toString())

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

           //return START_NOT_STICKY;
           //return START_STICKY;
             return START_REDELIVER_INTENT
    }


        private fun getMemUsage():Float{
           val mi=ActivityManager.MemoryInfo()
           am.getMemoryInfo(mi)
            val availmem=mi.availMem.toFloat()
            val memUsage:Float=(1-(availmem/ totalmem!!))*100

         return memUsage
         }

         override fun onBind(intent: Intent): IBinder {
           TODO("Return the communication channel to the service.")
         }
}