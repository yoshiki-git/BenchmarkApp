package com.example.benchmarkapp

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.util.Log
import android.view.ViewDebug
import androidx.core.content.ContextCompat.getSystemService
import java.util.*

class GetRamInfo(private val context: Context) {
    private val TAG="TestApp"
    var totalmemory:Float =0.0f
    var availmem:Float = 0.0f
    var usedmem:Float = 0.0f
    var ramUsage:Float = 0.0f
    private var am:ActivityManager = context.getSystemService(Service.ACTIVITY_SERVICE)as ActivityManager

    init {
        //ActivityManager取得
        //メモリの最大値を取得している
        val meminfo=ActivityManager.MemoryInfo()
        am.getMemoryInfo(meminfo)
        totalmemory=meminfo.totalMem.toFloat()/1024/1024
        Log.d(TAG,"TotalMemory: $totalmemory"+"MB")
    }

    fun update_property(){
        val mi=ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        availmem=mi.availMem.toFloat()/1024/1024
        usedmem=totalmemory - availmem
        ramUsage=usedmem/totalmemory
        Log.d(TAG,"TotalMemory: $totalmemory"+"MB")
        Log.d(TAG,"AvailMem: $availmem"+"MB")
        Log.d(TAG,"UsedMem: $usedmem"+"MB")
        Log.d(TAG, "Ram Usage: $ramUsage%")
    }



}