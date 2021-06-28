package com.example.benchmarkapp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.StatFs
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.util.Log
import java.util.*

class GetRomInfo(private val context: Context) {
    private val TAG = "TestApp"
    private var storageManager = context.getSystemService(StorageManager::class.java)

    @SuppressLint("DiscouragedPrivateApi")
    private fun getRomUsage(){


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
}