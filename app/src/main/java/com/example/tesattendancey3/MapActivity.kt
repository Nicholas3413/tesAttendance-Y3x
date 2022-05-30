package com.example.tesattendancey3

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_map.*
import java.lang.Math.cos


class MapActivity : AppCompatActivity() {

    private var locationRequest: LocationRequest? = null
    private var xlatitude:String?=""
    private var xlongitude:String?=""
    private var new_latitude_pos:Double?=0.00
    private var new_latitude_min:Double?=0.00
    private var new_longitude_pos:Double?=0.00
    private var new_longitude_min:Double?=0.00
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        locationRequest = LocationRequest.create();
        locationRequest?.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest?.setInterval(5000);
        locationRequest?.setFastestInterval(2000);
        locationButton.setOnClickListener {
            getCurrentLocation()
            locationMapButton.visibility=View.VISIBLE
        }
        locationMapButton.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:"+xlatitude+","+xlongitude)
            Log.v("ceklatlong",xlatitude+","+xlongitude)
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            mapIntent.resolveActivity(packageManager)?.let {
                startActivity(mapIntent)
            }
        }
        btnHitungMeter.setOnClickListener {
            var meter:Double=etxtMasukMeter.getText().toString().toDouble()
            var minusmeter=meter*-1
            val earth = 6378.137
            //radius of the earth in kilometer
            val pi = Math.PI
            val m:Double = 1 / (2 * pi / 360 * earth) / 1000 //1 meter in degree
            new_latitude_pos= xlatitude?.toDouble()?.plus(meter*m)
            new_latitude_min= xlatitude?.toDouble()?.plus(minusmeter*m)
            new_longitude_pos= xlongitude?.toDouble()?.plus(meter * m / cos(xlatitude!!.toDouble()* (pi / 180)))
            new_longitude_min= xlongitude?.toDouble()?.plus(minusmeter * m / cos(xlatitude!!.toDouble()* (pi / 180)))
            textNewLatLong.setText(new_latitude_pos.toString()+","+new_longitude_pos.toString()+","+new_latitude_min.toString()+","+new_longitude_min)
        }
        btnKonfirmasiLokasiCekIn.setOnClickListener {
            val sharedPreferences = getSharedPreferences("Location", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("new_latitude_pos",new_latitude_pos.toString())
            editor.putString("new_latitude_min",new_latitude_min.toString())
            editor.putString("new_longitude_pos",new_longitude_pos.toString())
            editor.putString("new_longitude_min",new_longitude_min.toString())
            editor.apply()
            finish()
        }



    }
    private fun getCurrentLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    this@MapActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (isGPSEnabled()) {
                    LocationServices.getFusedLocationProviderClient(this@MapActivity)
                        .requestLocationUpdates(locationRequest, object : LocationCallback() {
                            override fun onLocationResult(locationResult: LocationResult) {
                                super.onLocationResult(locationResult)
                                LocationServices.getFusedLocationProviderClient(this@MapActivity)
                                    .removeLocationUpdates(this)
                                if (locationResult != null && locationResult.locations.size > 0) {
                                    val index = locationResult.locations.size - 1
                                    val latitude = locationResult.locations[index].latitude
                                    xlatitude=latitude.toString()
                                    val longitude = locationResult.locations[index].longitude
                                    xlongitude=longitude.toString()
                                    addressText.setText("Latitude: $latitude\nLongitude: $longitude")

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
                Toast.makeText(this@MapActivity, "GPS is already tured on", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: ApiException) {
                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvableApiException = e as ResolvableApiException
                        resolvableApiException.startResolutionForResult(this@MapActivity, 2)
                    } catch (ex: SendIntentException) {
                        ex.printStackTrace()
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                }
            }
        })
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                getCurrentLocation()
            }
        }
    }

}

