package com.example.tesattendancey3

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.util.Pair
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.example.tesattendancey3.SimilarityClassifier.Recognition
import com.example.tesattendancey3.databinding.ActivityMainBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetector
import kotlinx.android.synthetic.main.activity_main.*
import org.tensorflow.lite.Interpreter
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity(), MainCallBack {
    private var binding: ActivityMainBinding? = null
    private var viewModel: MainViewModel? = null
    private var tfLite: Interpreter? = null
    private var detector: FaceDetector? = null
    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cam_face = CameraSelector.LENS_FACING_BACK
    private var registered = HashMap<String?, Recognition?>()
    private var flipX = false
    private var start = true
    private lateinit var embeddings: Array<FloatArray>
    private lateinit var auth: FirebaseAuth
    private var counter=0
    private lateinit var database: DatabaseReference
    private lateinit var tanggal:String


    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var locationRequest: LocationRequest

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel!!.init(this)
        binding?.setContract(this)
        binding?.setViewModel(viewModel)
        checkCameraPermission()
        registered = viewModel!!.readFromSP()!!
        try {
            tfLite = viewModel!!.getModel(this@MainActivity)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        detector = viewModel!!.detector
        cameraBind()
        btnRegisterMukaUntukSave.setOnClickListener {
            counter=counter+1
            Log.v("cekcounter",counter.toString())
            textSavedRecog.setText(counter.toString()+"/10")
            addFace(counter)
            if(counter==10){
                start=false
                finish()
            }
        }
        database = Firebase.database.reference
        database.child("timestamp").setValue(ServerValue.TIMESTAMP)
        database.child("timestamp").get().addOnSuccessListener {
            var timestamp=it.value
            tanggal=getDate(timestamp as Long)
            Log.v("checkpointx",tanggal)
        }.addOnFailureListener{
            Log.e("timestampfromdatabase", "Error getting data", it)
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setInterval(5000)
        locationRequest.setFastestInterval(1000)
        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult != null) {
                    if (locationResult == null) {
                        return
                    }
                    //Showing the latitude, longitude and accuracy on the home screen.
                    for (location in locationResult.locations) {
                        Log.v("newlocation","Lat: ${location.latitude} Long: ${location.longitude} Accuracy: ${location.accuracy}")
                        database = Firebase.database.reference
                        var userID=Firebase.auth.currentUser?.uid.toString()
                        if (loclongmin != null && loclamin != null && loclapos != null && loclongpos != null) {
                            if (location.latitude < loclapos!!.toDouble() && location.latitude > loclamin!!.toDouble()
                                && location.longitude < loclongpos!!.toDouble() && location.longitude > loclongmin!!.toDouble()
                            ) {
                                database.child(userID).child("perusahaan_id").get().addOnSuccessListener {
                                    perusahaanID=it.value.toString()
                                    database.child(it.value.toString())
                                        .child("absensi").child(tanggal).child(userID)
                                        .child("lokasi")
                                        .setValue(location.latitude.toString() + "," + location.longitude.toString())
                                    database.child(it.value.toString())
                                        .child("absensi").child(tanggal).child(userID)
                                        .child("ceklokasi")
                                        .setValue("berada pada lokasi")
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Sudah Berada di Lokasi Perusahaan",
                                        Toast.LENGTH_SHORT)
                                    Log.v("perusahaanid",perusahaanID.toString())
                                }.addOnFailureListener{
                                    Log.e("perusahaanid", "Error getting data", it)
                                }


                            } else {
                                database.child(userID).child("perusahaan_id").get().addOnSuccessListener {
                                    database.child(it.value.toString())
                                        .child("absensi").child(tanggal)
                                        .child(userID)
                                        .child("lokasi")
                                        .setValue(location.latitude.toString() + "," + location.longitude.toString())
                                    database.child(it.value.toString())
                                        .child("absensi").child(tanggal)
                                        .child(userID)
                                        .child("ceklokasi")
                                        .setValue("tidak pada lokasi")
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Tidak Berada di Lokasi Perusahaan",
                                        Toast.LENGTH_SHORT
                                    )
                                }.addOnFailureListener{
                                    Log.e("perusahaanid", "Error getting data", it)
                                }
                            }
                        }

                    }
                }
            }
        }
        fusedLocationProviderClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

    }

    override fun onStart() {
        super.onStart()
//        val intentextra:String = intent.getStringExtra("action").toString()
//        if(intentextra=="1"){
//            cekLocation()
//        }

//        val preferences = getSharedPreferences("HashMap", 0)
//        preferences.edit().remove("map").apply()
//        val sharedPreferences = getSharedPreferences("HashMap", Context.MODE_PRIVATE)
//        val editor = sharedPreferences.edit()
//        editor.clear().commit()
//        database = Firebase.database.reference
//        var userID=Firebase.auth.currentUser?.uid.toString()
//
//        database.child(userID).child("registered_face").get().addOnSuccessListener {
//            if(it.value!=null){
//            Log.v("inidaftarrecognya",it.value.toString())
////            registered= convertToHashMap(it.value.toString())
//////                Log.v("iniisiregisterednya",registered["zdrmlp1173"])
////            viewModel!!.insertToSP(registered, false, viewModel!!.readFromSP())
//            val sharedPreferences = getSharedPreferences("HashMap", Context.MODE_PRIVATE)
//            val editor = sharedPreferences.edit()
//                editor.clear().apply()
//                editor.putString("map", it.value.toString())
//                editor.apply()
//            }
//            else{
//                val sharedPreferences = getSharedPreferences("HashMap", Context.MODE_PRIVATE)
//                val editor = sharedPreferences.edit()
//                editor.clear().apply()
//            }
//        }.addOnFailureListener{
//
//        }




    }
    fun convertToHashMap(jsonString: String?): HashMap<String?, Recognition?> {
        var myHashMap = HashMap<String?, Recognition?>()
        Log.v("inikeystringnya",jsonString!!)
        val token: TypeToken<java.util.HashMap<String?, Recognition?>?> =
            object : TypeToken<java.util.HashMap<String?, Recognition?>?>() {}
        val retrievedMap = Gson().fromJson<java.util.HashMap<String?, Recognition?>?>(jsonString, token.type)
        myHashMap.putAll(retrievedMap)
//        try {
//            val jArray = JSONArray(jsonString)
//            var jObject: JSONObject? = null
//            var keyString: String? = null
//            for (i in 0 until jArray.length()) {
//                jObject = jArray.getJSONObject(i)
//                keyString = jObject.names()[i] as String
//                Log.v("inikeystringnya",keyString)
//                jObject.getString(keyString)
////                val defValue = Gson().toJson(java.util.HashMap<String?, Recognition?>())
//
//                val token: TypeToken<java.util.HashMap<String?, Recognition?>?> =
//                    object : TypeToken<java.util.HashMap<String?, Recognition?>?>() {}
//                val retrievedMap = Gson().fromJson<java.util.HashMap<String?, Recognition?>?>(keyString, token.type)
////                var jxArray=JSONArray(jObject)
////                for(j in 0 until jxArray.length()){
////                    var jxObject=jxArray.getJSONObject(j)
////                    when (j) {
////                        1 -> reg.
////                        2 -> print("x == 2")
////                        else -> {
////                            print("x is neither 1 nor 2")
////                        }
////                    }
////                }
////                keyString = jObject.names()[i] as String
//                myHashMap.putAll(retrievedMap)
//            }
//        } catch (e: JSONException) {
//            e.printStackTrace()
//        }
        return myHashMap
    }

    override fun onActionClick() {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Select Action:")
        val names = arrayOf(
            "View Recognition List",
            "Save Recognitions",
            "Load Recognitions",
            "Clear All Recognitions"
        )
        builder.setItems(
            names
        ) { _: DialogInterface?, which: Int ->
            when (which) {
                0 -> displayNameListView()
                1 -> viewModel!!.insertToSP(registered, false, viewModel!!.readFromSP())
                2 -> viewModel!!.readFromSP()?.let { registered.putAll(it) }
                3 -> clearNameList()
            }
        }
        var dialog = builder.create()
        dialog.show()
    }

    override fun onCameraSwitchClick() {
        if (cam_face == CameraSelector.LENS_FACING_BACK) {
            cam_face = CameraSelector.LENS_FACING_FRONT
            flipX = true
        } else {
            cam_face = CameraSelector.LENS_FACING_BACK
            flipX = false
        }
        cameraProvider!!.unbindAll()
        cameraBind()
    }

    @SuppressLint("SetTextI18n")
    override fun onRecognizeClick() {
//        if (binding?.recognize?.getText().toString().equals("Recognize")) {
//            start = true
//            viewModel!!.view_call(1)
//        } else {
//            viewModel!!.view_call(2)
//        }
    }

    override fun onAddFaceClick() {
//        addFace()
    }

    private fun cameraBind() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture!!.addListener({
            try {
                cameraProvider = cameraProviderFuture!!.get()
                bindPreview(cameraProvider!!)
            } catch (ignored: ExecutionException) {
            } catch (ignored: InterruptedException) {
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder().build()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(cam_face)
            .build()
        val imageAnalysis: ImageAnalysis = ImageRecognitionUtils.getImageAnalysis(preview, binding!!)
        val executor: Executor = Executors.newSingleThreadExecutor()
        imageAnalysis.setAnalyzer(executor) { imageProxy: ImageProxy ->
            var image: InputImage? = null

            @SuppressLint("UnsafeExperimentalUsageError") val mediaImage =
                imageProxy.image
            if (mediaImage != null) {
                image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )
            }
            assert(image != null)
            @SuppressLint("SetTextI18n") val result =
                detector!!.process(image!!).addOnSuccessListener { faces: List<Face> ->
                    if (faces.size != 0) {
                        val face = faces[0]
                        val frame_bmp: Bitmap = BitmapUtils.toBitmap(mediaImage!!)
                        val rot = imageProxy.imageInfo.rotationDegrees
                        val frame_bmp1: Bitmap =
                            BitmapUtils.rotateBitmap(frame_bmp, rot, false, false)
                        val boundingBox = RectF(face.boundingBox)
                        var cropped_face: Bitmap =
                            BitmapUtils.getCropBitmapByCPU(frame_bmp1, boundingBox)
                        if (flipX) {
                            cropped_face =
                                BitmapUtils.rotateBitmap(cropped_face, 0, true, false)
                        }
                        val scaled: Bitmap =
                            BitmapUtils.getResizedBitmap(cropped_face, 112, 112)
                        if (start) {
                            recognizeImage(scaled, binding, tfLite, registered)
                        }
                        try {
                            Thread.sleep(10)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    } else {
                        if (registered.isEmpty()) {
                            viewModel!!.recoNameText.set("Add Face")
                        } else {
                            viewModel!!.recoNameText.set("No Face Detected")
                        }
                    }
                }
                    .addOnCompleteListener { task: Task<List<Face?>?>? -> imageProxy.close() }
        }
        cameraProvider.bindToLifecycle(
            (this as LifecycleOwner),
            cameraSelector,
            imageAnalysis,
            preview
        )
    }

    fun recognizeImage(
        bitmap: Bitmap?,
        binding: ActivityMainBinding?,
        tfLite: Interpreter?,
        registered: HashMap<String?, Recognition?>
    ) {
        val inputSize = 112
        val OUTPUT_SIZE = 192
        val IMAGE_MEAN = 128.0f
        val IMAGE_STD = 128.0f
        binding?.facePreview?.setImageBitmap(bitmap)
        val imgData: ByteBuffer =
            ImageRecognitionUtils.getImgData(inputSize, bitmap!!, IMAGE_MEAN, IMAGE_STD)
        val inputArray = arrayOf<Any>(imgData)
        val outputMap: MutableMap<Int, Any> = HashMap()
        embeddings = Array(1) { FloatArray(OUTPUT_SIZE) }
        Log.v("xtesimgdatajjjj",imgData.toString())
        Log.v("xtesimgdataeeee",embeddings[0][0].toString())
        outputMap[0] = embeddings
        Log.v("xtesimgdataeeooo", outputMap[0].toString())
        tfLite!!.runForMultipleInputsOutputs(inputArray, outputMap)
        Log.v("xtesimgdataeeeex",embeddings[0][0].toString())
        val intentextra:String = intent.getStringExtra("action").toString()
        if(intentextra=="1"){
            val distance: Float
            if (registered.size > 0) {
                val nearest: Pair<String, Float>? = ImageRecognitionUtils.findNearest(
                    embeddings[0], registered
                )
                if (nearest != null) {
                    val name = nearest.first
                    distance = nearest.second
                    Log.v("cekdistance",distance.toString())
                    if (distance < 0.600f) {
                        Toast.makeText(this@MainActivity, name, Toast.LENGTH_SHORT).show()
                        viewModel!!.recoNameText.set(name)
                        auth= Firebase.auth
                        var userID= Firebase.auth.currentUser?.uid.toString()
                        database = Firebase.database.reference
                        var perusahaanId=""
                        database.child(userID).child("perusahaan_id").get().addOnSuccessListener {
                            perusahaanId=it.value.toString()
                            Log.v("iniperusahaanId",perusahaanId)
                            if(perusahaanId!="null"){
                                Log.v("checkpoint",tanggal)
                                database.child(perusahaanId).child("absensi").child(tanggal).child(userID).child("jam_masuk").setValue(ServerValue.TIMESTAMP)
                            }
                            else{
                                database.child(userID).child("anggota_perusahaan_id").get().addOnSuccessListener {
                                    database.child(it.value.toString()).child("absensi").child(tanggal).child(userID).child("jam_masuk").setValue(ServerValue.TIMESTAMP)
                                }
                            }
                        }.addOnFailureListener {

                        }
                        finish()
                    } else {
                        viewModel!!.recoNameText.set("Unknown")
                    }
                }
            }
        }
    }

    private fun cekLocation(){
        database = Firebase.database.reference
        var userID=Firebase.auth.currentUser?.uid.toString()
        database.child(userID).child("perusahaan_id").get().addOnSuccessListener {
            perusahaanID=it.value.toString()
            database.child(perusahaanID).get().addOnSuccessListener {
                loclapos=it.child("loclapos").value.toString().toDouble()
                loclamin=it.child("loclamin").value.toString().toDouble()
                loclongpos=it.child("loclongpos").value.toString().toDouble()
                loclongmin=it.child("loclongmin").value.toString().toDouble()
            }.addOnFailureListener {
                Log.e("perusahaanID", "Error getting data", it)
            }
            Log.v("perusahaanid",perusahaanID.toString())
        }.addOnFailureListener{
            Log.e("perusahaanid", "Error getting data", it)
        }

        locationRequest = LocationRequest.create()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setInterval(100)
        locationRequest.setFastestInterval(50)
        getCurrentLocation()
    }

    private lateinit var perusahaanID:String
    private var loclapos:Double?=0.00
    private var loclamin:Double?=0.00
    private var loclongpos:Double?=0.00
    private var loclongmin:Double?=0.00
    private fun getCurrentLocation() {
        database = Firebase.database.reference
        var userID=Firebase.auth.currentUser?.uid.toString()

//        database.child(perusahaanID!!).get().addOnSuccessListener {
//            loclapos=it.child("loclapos").value.toString().toDouble()
//            loclamin=it.child("loclamin").value.toString().toDouble()
//            loclongpos=it.child("loclongpos").value.toString().toDouble()
//            loclongmin=it.child("loclongmin").value.toString().toDouble()
//        }.addOnFailureListener {
//            Log.e("perusahaanID", "Error getting data", it)
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (isGPSEnabled()) {
                    LocationServices.getFusedLocationProviderClient(this@MainActivity)
                        .requestLocationUpdates(locationRequest, object : LocationCallback() {
                            override fun onLocationResult(locationResult: LocationResult) {
                                super.onLocationResult(locationResult)
                                LocationServices.getFusedLocationProviderClient(this@MainActivity)
                                    .removeLocationUpdates(this)
                                if (locationResult != null && locationResult.locations.size > 0) {
                                    val index = locationResult.locations.size - 1
                                    val latitude = locationResult.locations[index].latitude
                                    val longitude = locationResult.locations[index].longitude
                                    try {
                                        if (loclongmin != null && loclamin != null && loclapos != null && loclongpos != null) {
                                            if (latitude < loclapos!!.toDouble() && latitude > loclamin!!.toDouble()
                                                && longitude < loclongpos!!.toDouble() && longitude > loclongmin!!.toDouble()
                                            ) {
                                                database.child(userID).child("perusahaan_id").get().addOnSuccessListener {
                                                    perusahaanID=it.value.toString()
                                                    database.child(perusahaanID.toString())
                                                        .child("absensi").child(tanggal).child(userID)
                                                        .child("lokasi")
                                                        .setValue(latitude.toString() + "," + longitude.toString())
                                                    database.child(perusahaanID.toString())
                                                        .child("absensi").child(tanggal).child(userID)
                                                        .child("ceklokasi")
                                                        .setValue("berada pada lokasi")
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        "Sudah Berada di Lokasi Perusahaan",
                                                        Toast.LENGTH_SHORT)
                                                    Log.v("perusahaanid",perusahaanID.toString())
                                                }.addOnFailureListener{
                                                    Log.e("perusahaanid", "Error getting data", it)
                                                }


                                            } else {
                                                database.child(userID).child("perusahaan_id").get().addOnSuccessListener {
                                                    database.child(perusahaanID.toString())
                                                        .child("absensi").child(tanggal)
                                                        .child(userID)
                                                        .child("lokasi")
                                                        .setValue(latitude.toString() + "," + longitude.toString())
                                                    database.child(perusahaanID.toString())
                                                        .child("absensi").child(tanggal)
                                                        .child(userID)
                                                        .child("ceklokasi")
                                                        .setValue("tidak pada lokasi")
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        "Tidak Berada di Lokasi Perusahaan",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                }.addOnFailureListener{
                                                    Log.e("perusahaanid", "Error getting data", it)
                                                }
                                            }
                                        }
                                    }catch(e:NumberFormatException)
                                    {

                                        Log.e("numberformatnull",e.toString())
                                    }
                                }
                            }
                        }, Looper.getMainLooper())
                } else {
                    turnOnGPS()
                }
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }
    }
    private fun isGPSEnabled(): Boolean {
        var locationManager: LocationManager? = null
        var isEnabled = false
        if (locationManager == null) {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        return isEnabled
    }
    private fun turnOnGPS() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result: Task<LocationSettingsResponse> = LocationServices.getSettingsClient(
            applicationContext
        )
            .checkLocationSettings(builder.build())
        result.addOnCompleteListener(OnCompleteListener<LocationSettingsResponse?> { task ->
            try {
                val response = task.getResult(ApiException::class.java)
                Toast.makeText(this@MainActivity, "GPS is already tured on", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: ApiException) {
                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvableApiException = e as ResolvableApiException
                        resolvableApiException.startResolutionForResult(this@MainActivity, 2)
                    } catch (ex: IntentSender.SendIntentException) {
                        ex.printStackTrace()
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                }
            }
        })
    }





    private fun getDate(time: Long): String {
        val format = "dd MMM yyyy" // you can add the format you need
        val sdf = SimpleDateFormat(format, Locale.getDefault()) // default local
        sdf.timeZone = TimeZone.getDefault()
//        sdf.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        return sdf.format(Date(time))
    }

    private fun addFace(cout:Int) {
        auth= Firebase.auth
        var namaUser = auth.currentUser?.displayName.toString()
        var namaUserIterative=namaUser+cout.toString()
        Log.v("ceknamadisplayed",namaUser)
        val result = Recognition(
            cout.toString(), "", -1f
        )
        result.extra = embeddings
        registered[namaUserIterative] = result
        viewModel!!.insertToSP(registered, false, viewModel!!.readFromSP())


//        val builder = AlertDialog.Builder(this@MainActivity)
//        builder.setTitle("Enter Name")
//        val input = EditText(this@MainActivity)
//        input.inputType = InputType.TYPE_CLASS_TEXT
//        builder.setView(input)
//        builder.setPositiveButton("ADD") { dialog: DialogInterface?, which: Int ->
//            val result = Recognition(
//                "0", "", -1f
//            )
//            result.extra = embeddings
//            registered[input.text.toString()] = result
//            start = true
//        }
//        builder.setNegativeButton("Cancel") { dialog: DialogInterface, which: Int ->
//            start = true
//            dialog.cancel()
//        }
//        builder.show()
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun checkCameraPermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), MY_CAMERA_REQUEST_CODE)
        }
    }

    private fun clearNameList() {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Do you want to delete all Recognitions?")
        builder.setPositiveButton("Delete All") { dialog: DialogInterface?, which: Int ->
            registered.clear()
            Toast.makeText(this@MainActivity, "Recognitions Cleared", Toast.LENGTH_SHORT).show()
        }
        viewModel!!.insertToSP(registered, true, viewModel!!.readFromSP())
        builder.setNegativeButton("Cancel", null)
        val dialog = builder.create()
        dialog.show()
    }

    private fun displayNameListView() {
        val builder = AlertDialog.Builder(this@MainActivity)
        if (registered.isEmpty()) builder.setTitle("No Faces Added!!") else builder.setTitle("Recognitions:")
        val names = arrayOfNulls<String>(registered.size)
        val checkedItems = BooleanArray(registered.size)
        var i = 0
        for ((key) in registered) {

            names[i] = key
            checkedItems[i] = false
            i = i + 1
        }
        builder.setItems(names, null)
        val dialog = builder.create()
        dialog.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show()
            }
        }
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isGPSEnabled()) {
                    getCurrentLocation()
                } else {
                    turnOnGPS()
                }
            }
        }
    }

    companion object {
        private const val MY_CAMERA_REQUEST_CODE = 100
    }
}