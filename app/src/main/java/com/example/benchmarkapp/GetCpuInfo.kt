package com.example.benchmarkapp

import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class GetCpuInfo {
    private val TAG:String="TestApp"

    // CPUのコア数を求める。8コアだったら7(0からカウントしてるので) エラーの場合は-1を返す。　
    fun countCoreNum():Int{
        var coreNum=0
        for (i in 0..999) {
            val dir = File("sys/devices/system/cpu/cpu$i")
            if (!dir.exists()) {
                coreNum = i
                Log.d("coreNum", coreNum.toString())
                break
            }
        }
        return coreNum-1
    }


    //最小CPUクロックを求める。取得エラー時は0
    private fun takeMinCpuFreq(coreIndex:Int):Int{
        return readIntegerFile("/sys/devices/system/cpu/cpu$coreIndex/cpufreq/cpuinfo_min_freq")
    }

    fun takeMinCpuFreqs(coreNum:Int):IntArray{
        val minFreqs:IntArray= IntArray(coreNum)
        for(i in 0 until coreNum){
            minFreqs[i]=takeMinCpuFreq(i)
        }
        Log.d(TAG,"minFreqs:"+minFreqs.contentToString())

        return minFreqs
    }

    //最大CPUクロックを求める。取得エラー時は0
    private fun takeMaxCpuFreq(coreIndex: Int): Int {
        return readIntegerFile("/sys/devices/system/cpu/cpu$coreIndex/cpufreq/cpuinfo_max_freq")
    }

    fun takeMaxCpuFreqs(coreNum:Int):IntArray{
        val maxFreqs:IntArray= IntArray(coreNum)
        for(i in 0 until coreNum){
            maxFreqs[i]=takeMaxCpuFreq(i)
        }
        Log.d(TAG,"maxFreqs:"+maxFreqs.contentToString())
        return maxFreqs
    }

    //CPU使用率の計算
    fun getCpuUsages(coreNum: Int,minCpuFreqs:IntArray,maxCpuFreqs:IntArray,currentFreqs:IntArray):FloatArray{


        //CPU使用率の計算
        val cpuUsages:FloatArray= FloatArray(coreNum)
        for(i in 0 until coreNum){
            if ((maxCpuFreqs[i]-minCpuFreqs[i])==0){
                cpuUsages[i]=0f
            }else{
                cpuUsages[i]=(currentFreqs[i]-minCpuFreqs[i]).toFloat()/(maxCpuFreqs[i]-minCpuFreqs[i]).toFloat()*100
            }
        }
        return cpuUsages

    }



    //現在のCPUクロックを求める。取得エラー時は0
    private fun takeCurrentCpuFreq(coreIndex: Int): Int {
        return readIntegerFile("/sys/devices/system/cpu/cpu$coreIndex/cpufreq/scaling_cur_freq")
    }

    fun takeCurrentCpuFreqs(coreNum:Int):IntArray{
        val currentFreqs:IntArray= IntArray(coreNum)
        for(i in 0 until coreNum){
            currentFreqs[i]=takeCurrentCpuFreq(i)
        }
        Log.d(TAG,"currFreqs:"+currentFreqs.contentToString())

        return currentFreqs
    }


    private fun readIntegerFile(filePath: String): Int {

        try {
            BufferedReader(
                    InputStreamReader(FileInputStream(filePath)), 1000).use { reader ->

                val line = reader.readLine()
                return Integer.parseInt(line)
            }

        } catch (e: Exception) {

            // 冬眠してるコアのデータは取れないのでログを出力しない
            //MyLog.e(e);

            return 0
        }

    }

}