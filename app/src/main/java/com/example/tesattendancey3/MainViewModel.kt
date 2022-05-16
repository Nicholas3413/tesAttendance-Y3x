package com.example.tesattendancey3

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.example.tesattendancey3.SimilarityClassifier.Recognition
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class MainViewModel : ViewModel() {
    private var repo: MainRepo? = null
    private val addFaceVisibility: ObservableField<Int> = ObservableField()
    private val facePreviewVisibility: ObservableField<Int> = ObservableField()
    private val recoNameVisibility: ObservableField<Int> = ObservableField()
    private val previewInfoText: ObservableField<String> = ObservableField()
    private val recognizeText: ObservableField<String> = ObservableField()
    val recoNameText: ObservableField<String> = ObservableField()

    fun init(context: Context?) {
        repo = MainRepo.getInstance(context)
        addFaceVisibility.set(View.INVISIBLE)
        facePreviewVisibility.set(View.VISIBLE)
        previewInfoText.set("\n    Recognized Face:")
        recognizeText.set("Add Face")
    }

    fun readFromSP(): java.util.HashMap<String?, Recognition?>? {
        return repo!!.readFromSP()
    }

    fun insertToSP(
        jsonMap: HashMap<String?, Recognition?>?,
        clear: Boolean,
        registered: HashMap<String?, Recognition?>?
    ) {
        repo!!.insertToSP(jsonMap!!, clear, registered)
    }

    @Throws(IOException::class)
    fun getModel(activity: Activity): Interpreter {
        val modelFile = "mobile_face_net.tflite"
        return Interpreter(loadModelFile(activity, modelFile))
    }

    val detector: FaceDetector
        get() {
            val highAccuracyOpts = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .build()
            return FaceDetection.getClient(highAccuracyOpts)
        }

    @Throws(IOException::class)
    fun loadModelFile(activity: Activity, MODEL_FILE: String?): MappedByteBuffer {
        val fileDescriptor = activity.assets.openFd(MODEL_FILE!!)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun getAddFaceVisibility(): ObservableField<Int> {
        return addFaceVisibility
    }

    fun getFacePreviewVisibility(): ObservableField<Int> {
        return facePreviewVisibility
    }

    fun getRecoNameVisibility(): ObservableField<Int> {
        return recoNameVisibility
    }

    fun getPreviewInfoText(): ObservableField<String> {
        return previewInfoText
    }

    fun getRecognizeText(): ObservableField<String> {
        return recognizeText
    }

//    fun view_call(flag: Int) {
//        if (flag == 1) {
//            recognizeText.set("Add Face")
//            addFaceVisibility.set(View.INVISIBLE)
//            recoNameVisibility.set(View.VISIBLE)
//            facePreviewVisibility.set(View.INVISIBLE)
//            previewInfoText.set("\n    Recognized Face:")
//        } else if (flag == 2) {
//            recognizeText.set("Recognize")
//            addFaceVisibility.set(View.VISIBLE)
//            recoNameVisibility.set(View.INVISIBLE)
//            facePreviewVisibility.set(View.VISIBLE)
//            previewInfoText.set("1.Bring Face in view of Camera.\n\n2.Your Face preview will appear here.\n\n3.Click Add button to save face.")
//        }
//    }
}