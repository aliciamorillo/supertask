package com.example.supertask

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.etEmail
import kotlinx.android.synthetic.main.activity_main.etPass

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    val TAG = "miapp"

    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Modo sin conexión + va más rápido
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        val emailExistent = DataHolder.email
        if (emailExistent != "") {
            etEmail.setText(emailExistent)
        }

        btRegister.setOnClickListener {
            DataHolder.email = etEmail.text.toString()
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        btLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val pass = etPass.text.toString()

            if (isEmailValid(email) && pass.length >= 6) {
                loginUser(email, etPass.text.toString())
            } else {
                DataHolder.email = email
                Toast.makeText(baseContext, "Email o contraseña no valido", Toast.LENGTH_SHORT)
                    .show()
            }
        }


    }


    fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun loginUser(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser

                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser?.uid != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

    }
}
