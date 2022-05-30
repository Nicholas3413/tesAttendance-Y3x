package com.example.tesattendancey3

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.auth.User
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_buat_perusahaan.*
import java.util.*
import kotlin.random.Random

class BuatPerusahaan : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buat_perusahaan)
        val mTimePicker: TimePickerDialog
        val nTimePicker: TimePickerDialog
        val mcurrentTime = Calendar.getInstance()
        val hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)
        val minute = mcurrentTime.get(Calendar.MINUTE)
        var totjammasuk=0
        var totjampulang=0

        mTimePicker = TimePickerDialog(this, object : TimePickerDialog.OnTimeSetListener {
            override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                totjammasuk=(hourOfDay*60+minute)*60
                selectedJamMasuk.setText(String.format("%d : %d", hourOfDay, minute))
            }
        }, hour, minute, true)
        nTimePicker = TimePickerDialog(this, object : TimePickerDialog.OnTimeSetListener {
            override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                totjampulang=(hourOfDay*60+minute)*60
                selectedJamPulang.setText(String.format("%d : %d", hourOfDay, minute))
            }
        }, hour, minute, true)

        selectJamMasuk.setOnClickListener { v ->
            mTimePicker.show()
        }
        selectJamPulang.setOnClickListener {
            v-> nTimePicker.show()
        }
        btnRegisterPerusahaan.setOnClickListener {
            auth= Firebase.auth
            var userID=Firebase.auth.currentUser?.uid.toString()
            writeNewPerusahaan(userID,textNamaPerusahaan.getText().toString(),totjammasuk.toLong(),totjampulang.toLong())
            finish()
        }
        btnKeMap.setOnClickListener {
            var intent= Intent(this,MapActivity::class.java)
            startActivity(intent)
        }

    }


    fun writeNewPerusahaan(userId: String, name: String,jam_masuk: Long?,jam_pulang: Long? ) {
        database = Firebase.database.reference
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        var randomstring=""
        for (i in 1..6) {
            val random1 = (0..charPool.size).shuffled().last()
            var tempchar=charPool[random1]
            randomstring=randomstring+tempchar
        }
        Log.v("randomstring",randomstring)
//        val random1 = (0..100).shuffled().last()


//        database.child(userId).child("Perusahaan").setValue(perusahaan)
        val sharedPreferences = getSharedPreferences("Location", Context.MODE_PRIVATE)
        var loclapos=sharedPreferences.getString("new_latitude_pos","")
        var loclamin=sharedPreferences.getString("new_latitude_min","")
        var loclongpos=sharedPreferences.getString("new_longitude_pos","")
        var loclongmin=sharedPreferences.getString("new_longitude_min","")
        Log.v("4loc",loclapos.toString()+loclamin.toString()+loclongpos.toString()+loclongmin.toString())
        val perusahaan = Perusahaan(name,jam_masuk,jam_pulang,randomstring,userId,loclapos,loclamin,loclongpos,loclongmin)
        database.child(randomstring).setValue(perusahaan)
        database.child(userId).child("perusahaan_id").setValue(randomstring)
    }
    @IgnoreExtraProperties
    data class Perusahaan(val nama_perusahaan: String? = null, val jam_masuk: Long? = null, val jam_pulang: Long? = null,
                          val inv_code:String?=null, val pemilik_id:String?=null,val loclapos:String?=null,val loclamin:String?=null
                          ,val loclongpos:String?=null,val loclongmin:String?=null) {
    }
}