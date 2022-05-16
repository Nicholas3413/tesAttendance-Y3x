package com.example.tesattendancey3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_menu_register_login.*
import java.text.SimpleDateFormat
import java.util.*

class MenuRegisterLogin : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_register_login)

        btnRegister.setOnClickListener {
            database.child("timestamp").setValue(ServerValue.TIMESTAMP)
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        database = Firebase.database.reference

        //untuk read jam yang telah disimpan
        database.child("timestamp").get().addOnSuccessListener {
            Log.i("timestampfromdatabase", "Got value ${it.value}")
            var timestamp=it.value
            textTesDate.setText(getDateTime(timestamp as Long)).toString()
        }.addOnFailureListener{
            Log.e("timestampfromdatabase", "Error getting data", it)
        }
    }
    private fun getDateTime(time: Long): String {
        val format = "dd MMM yyyy HH:mm:ss" // you can add the format you need
        val sdf = SimpleDateFormat(format, Locale.getDefault()) // default local
        sdf.timeZone = TimeZone.getDefault()
//        sdf.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        return sdf.format(Date(time))
    }
}