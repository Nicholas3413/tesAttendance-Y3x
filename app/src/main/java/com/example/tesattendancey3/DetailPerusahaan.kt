package com.example.tesattendancey3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_buat_perusahaan.*
import kotlinx.android.synthetic.main.activity_detail_perusahaan.*
import kotlinx.android.synthetic.main.activity_menu_register_login.*

class DetailPerusahaan : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_perusahaan)
        auth= Firebase.auth
        var userID=Firebase.auth.currentUser?.uid.toString()
        database = Firebase.database.reference
        var perusahaanId=""
        database.child(userID).child("perusahaan_id").get().addOnSuccessListener {  perusahaanId=it.value.toString()
        Log.v("iniperusahaanId",perusahaanId)
            if(perusahaanId!="null"){
                database.child(perusahaanId).get().addOnSuccessListener {
                    Log.i("invcode", "Got value ${it.value}")
                    textInvCodeDetail.setText(it.child("inv_code").value.toString())
                    textNamaPerusahaanDetail.setText(it.child("nama_perusahaan").value.toString())
                    textJamMasukDetail.setText(it.child("jam_masuk").value.toString())
                    textJamPulangDetail.setText(it.child("jam_pulang").value.toString())
                    var namaPemilik=it.child("pemilik_id").value.toString()
                    database.child(namaPemilik).child("nama_user").get().addOnSuccessListener {
                        textNamaPemilikDetail.setText(it.value.toString())
                    }

                }.addOnFailureListener{
                    }
            }
            else{
                database.child(userID).child("anggota_perusahaan_id").get().addOnSuccessListener {
                    database.child(it.value.toString()).get().addOnSuccessListener {
                        Log.i("invcode", "Got value ${it.value}")
                        textInvCodeDetail.setText(it.child("inv_code").value.toString())
                        textNamaPerusahaanDetail.setText(it.child("nama_perusahaan").value.toString())
                        textJamMasukDetail.setText(it.child("jam_masuk").value.toString())
                        textJamPulangDetail.setText(it.child("jam_pulang").value.toString())
                        var namaPemilik=it.child("pemilik_id").value.toString()
                        database.child(namaPemilik).child("nama_user").get().addOnSuccessListener {
                            textNamaPemilikDetail.setText(it.value.toString())
                        }
                    }.addOnFailureListener{
                    }
                }
            }
        }.addOnFailureListener {

        }
    }
    @IgnoreExtraProperties
    data class Perusahaan(val nama_perusahaan: String? = null, val jam_masuk: Long? = null, val jam_pulang: Long? = null,val inv_code:String?=null) {
    }
}