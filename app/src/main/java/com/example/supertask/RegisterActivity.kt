package com.example.supertask

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.supertask.models.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    val TAG = "RegisterActivity"
    private lateinit var auth: FirebaseAuth
    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        btRegisterNew.setOnClickListener {

            if(etPass.text.length >= 7){
                registerUser(etEmail.text.toString(), etPass.text.toString())
            } else {
                Toast.makeText(this, "La contraseña tiene que tener mas de 7 caracteres", Toast.LENGTH_SHORT)
            }
        }

        btBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    fun registerUser(email: String, pass: String) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser

                    // Guardar datos de usuario
                    val newUser = Users()
                    newUser.name = etName.text.toString()
                    newUser.surname = etSurname.text.toString()
                    newUser.age = etAge.text.toString().toInt()
                    newUser.email = email

                    db.collection("users").document(auth.currentUser?.uid.toString())
                        .set(newUser)
                        .addOnSuccessListener { documentReference ->
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error adding document", e)
                        }


                    //Llevar a HOME
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener {
                Log.e(TAG,it.toString() )
            }
    }
}
