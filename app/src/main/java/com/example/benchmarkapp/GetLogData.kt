package com.example.benchmarkapp

import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

class GetLogData(val context: Context) {
    val TAG="TestApp"

    fun writeLogData(){

    }

    fun getFileStatus(fileName:String):File{
        val dir_myApp=File("/sdcard/端末負荷アプリ")
        if (dir_myApp.exists()){
            Log.d(TAG,"App's dir is exist")
        }else{
            dir_myApp.mkdir()
            Log.d(TAG,"Made App's dir")
        }
        return File(dir_myApp,fileName)
    }

    fun getLog(filepath: File, log_data: String) {
        //ログを外部ストレージに保存する
        if (isExternalStorageWritable()) {
            try {
                FileOutputStream(filepath, true).use { fileOutputStream ->
                    OutputStreamWriter(
                        fileOutputStream,
                        StandardCharsets.UTF_8
                    ).use { outputStreamWriter ->
                        BufferedWriter(outputStreamWriter).use { bw ->
                            bw.write(log_data)
                            bw.flush()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Exception")
                e.printStackTrace()
                Toast.makeText(context,"ストレージが見つからないためログを保存できません。", Toast.LENGTH_LONG).show()
                Log.d("TAG", "トースト表示")
            }
        }
    }



    fun isExternalStorageWritable():Boolean{
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }
}