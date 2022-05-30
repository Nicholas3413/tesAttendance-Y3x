package com.example.tesattendancey3

import android.graphics.Bitmap
import android.util.Log
import android.util.Pair
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import com.example.tesattendancey3.SimilarityClassifier.Recognition
import com.example.tesattendancey3.databinding.ActivityMainBinding
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.HashMap


object ImageRecognitionUtils {
    fun findNearest(
        emb: FloatArray,
        registered: HashMap<String?, Recognition?>
    ): Pair<String, Float>? {
        var ret: Pair<String, Float>? = null
        for ((name, value) in registered) {
            val knownEmb = (value?.extra as Array<FloatArray>?)!![0]
            var distance = 0f
            for (i in emb.indices) {
                val diff = emb[i] - knownEmb[i]
                distance += diff * diff
            }
            distance = Math.sqrt(distance.toDouble()).toFloat()
            if (ret == null || distance < ret.second) {
                ret = Pair(name, distance)
            }
        }
        return ret
    }

    fun getImageAnalysis(preview: Preview, binding: ActivityMainBinding): ImageAnalysis {
        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider())
        return ImageAnalysis.Builder()
            .setTargetResolution(Size(640, 480))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }

    fun getImgData(
        inputSize: Int,
        bitmap: Bitmap,
        IMAGE_MEAN: Float,
        IMAGE_STD: Float
    ): ByteBuffer {
        val imgData = ByteBuffer.allocateDirect(inputSize * inputSize * 3 * 4)
        imgData.order(ByteOrder.nativeOrder())
        Log.v("xtesimgdata",imgData.toString())
        val intValues = IntArray(inputSize * inputSize)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        imgData.rewind()
        var ccc=0
        var recordBitmap:String?=""
        var recordRGB:String?=""
        var recordRGBminus:String?=""
        var recordRGBbagi:String?=""
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                ccc=ccc+12
                val pixelValue = intValues[i * inputSize + j]
                if(i<10&&j<10) {
                    recordBitmap=recordBitmap+"["
                    recordBitmap=recordBitmap+pixelValue.toString()+"],"
                    recordRGB=recordRGB+"["
                    recordRGB=recordRGB+(pixelValue shr 16 and 0xFF).toString()+","
                    recordRGB=recordRGB+(pixelValue shr 8 and 0xFF).toString()+","
                    recordRGB=recordRGB+(pixelValue and 0xFF).toString()+"],"
                    recordRGBminus=recordRGBminus+"["
                    recordRGBminus=recordRGBminus+((pixelValue shr 16 and 0xFF)-128).toString()+","
                    recordRGBminus=recordRGBminus+((pixelValue shr 8 and 0xFF)-128).toString()+","
                    recordRGBminus=recordRGBminus+((pixelValue and 0xFF)-128).toString()+"],"
                    recordRGBbagi=recordRGBbagi+"["
                    recordRGBbagi=recordRGBbagi+(((pixelValue shr 16 and 0xFF)-128)/128f).toFloat().toString()+","
                    recordRGBbagi=recordRGBbagi+(((pixelValue shr 8 and 0xFF)-128)/128f).toFloat().toString()+","
                    recordRGBbagi=recordRGBbagi+(((pixelValue and 0xFF)-128)/128f).toFloat().toString()+"],"
//                    Log.v("xtesimgdatapixel-1", (pixelValue).toString())
//                    Log.v("xtesimgdatapixel0", (pixelValue shr 16).toString())
//                    Log.v("xtesimgdatapixel1", (pixelValue shr 16 and 0xFF).toString())
//                    Log.v(
//                        "xtesimgdatapixel2",
//                        ((pixelValue shr 16 and 0xFF) - IMAGE_MEAN).toString()
//                    )
//                    Log.v(
//                        "xtesimgdatapixel3",
//                        (((pixelValue shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD).toString()
//                    )
//                    Log.v("xtesimgdatapixely-1", (pixelValue).toString())
//                    Log.v("xtesimgdatapixely0", (pixelValue shr 8).toString())
//                    Log.v("xtesimgdatapixely1", (pixelValue shr 8 and 0xFF).toString())
//                    Log.v(
//                        "xtesimgdatapixely2",
//                        ((pixelValue shr 8 and 0xFF) - IMAGE_MEAN).toString()
//                    )
//                    Log.v(
//                        "xtesimgdatapixely3",
//                        (((pixelValue shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD).toString()
//                    )
//                    Log.v("xtesimgdatapixelz-1", (pixelValue).toString())
//                    Log.v("xtesimgdatapixelz1", (pixelValue and 0xFF).toString())
//                    Log.v(
//                        "xtesimgdatapixelz2",
//                        ((pixelValue and 0xFF) - IMAGE_MEAN).toString()
//                    )
//                    Log.v(
//                        "xtesimgdatapixelz3",
//                        (((pixelValue and 0xFF) - IMAGE_MEAN) / IMAGE_STD).toString()
//                    )
                }
                imgData.putFloat(((pixelValue shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                imgData.putFloat(((pixelValue shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                imgData.putFloat(((pixelValue and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
            }
        }
        Log.v("recordBitmap",recordBitmap.toString())
        Log.v("recordRGB",recordRGB.toString())
        Log.v("recordRGBminus",recordRGBminus.toString())
        Log.v("recordRGBbagi",recordRGBbagi.toString())
        Log.v("ccc",ccc.toString())
        return imgData
    }
}