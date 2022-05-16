package com.example.tesattendancey3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_masuk_perusahaan.*

class MasukPerusahaan : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_masuk_perusahaan)
        auth= Firebase.auth
        var userID= Firebase.auth.currentUser?.uid.toString()
        database = Firebase.database.reference
        btnMasukKodeInvitasi.setOnClickListener {
            database.child(etxtMasukKode.getText().toString()).child("anggota").child(userID)
                .setValue(
                    ServerValue.TIMESTAMP
                )
            database.child(userID).child("anggota_perusahaan_id").setValue(etxtMasukKode.getText().toString())
        }
    }
}