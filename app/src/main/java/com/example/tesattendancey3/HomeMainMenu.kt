package com.example.tesattendancey3

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home_main_menu.*

class HomeMainMenu : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_main_menu)
        btnRegristrasiMuka.setOnClickListener {
            var intent= Intent(this,MainActivity::class.java)
            intent.putExtra("action", "0")
            startActivity(intent)
        }
        btnCheckIn.setOnClickListener {
            var intent= Intent(this,MainActivity::class.java)
            intent.putExtra("action", "1")
            startActivity(intent)
        }
        btnBuatPerusahaan.setOnClickListener {
            var intent= Intent(this,BuatPerusahaan::class.java)
            startActivity(intent)
        }
        btnLogOut.setOnClickListener {
            auth=Firebase.auth
            Firebase.auth.signOut()
            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show()
            finish()
        }
        btnDetailPerusahaan.setOnClickListener {
            var intent= Intent(this,DetailPerusahaan::class.java)
            startActivity(intent)
        }
        btnMasukPerusahaan.setOnClickListener {
            var intent= Intent(this,MasukPerusahaan::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()

        database = Firebase.database.reference
        var userID=Firebase.auth.currentUser?.uid.toString()
        var userName=Firebase.auth.currentUser?.displayName.toString()
        database.child(userID).child("nama_user").setValue(userName)
        database = Firebase.database.reference

        database.child(userID).child("registered_face").get().addOnSuccessListener {
            if(it.value!=null){
                Log.v("inidaftarrecognya",it.value.toString())
//            registered= convertToHashMap(it.value.toString())
////                Log.v("iniisiregisterednya",registered["zdrmlp1173"])
//            viewModel!!.insertToSP(registered, false, viewModel!!.readFromSP())
                val sharedPreferences = getSharedPreferences("HashMap", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.clear().apply()
                editor.putString("map", it.value.toString())
                editor.apply()
            }
            else{
                val sharedPreferences = getSharedPreferences("HashMap", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.clear().apply()
            }
        }.addOnFailureListener{

        }
    }
}