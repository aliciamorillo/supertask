package com.example.supertask

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.supertask.models.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    val TAG = " ProfileActivity"
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var idUnicoUsuarioActual = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        idUnicoUsuarioActual = auth.currentUser?.uid.toString()
        db.collection("users").document(idUnicoUsuarioActual).get()
            .addOnSuccessListener { document ->
                if (document != null) {

                    val miUsuario = document.toObject(Users::class.java)
                    etName.setText(miUsuario?.name)
                    etSurname.setText(miUsuario?.surname)
                    etAge.setText(miUsuario?.age.toString())
                    etEmail.setText(miUsuario?.email)

                } else {

                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

        btSave.setOnClickListener {
            actualizarDatos()
        }
    }

    fun actualizarDatos() {
        val upUser = Users()

        upUser.name = etName.text.toString()
        upUser.surname = etSurname.text.toString()
        upUser.age = etAge.text.toString().toInt()
        upUser.email = etEmail.text.toString()

        db.collection("users").document(idUnicoUsuarioActual).set(upUser)
            .addOnCompleteListener {
                Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}
