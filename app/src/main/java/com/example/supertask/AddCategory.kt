package com.example.supertask

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.supertask.models.Category
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_add_category.*

class AddCategory : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_category)

        btSaveCategory.setOnClickListener {
            var newCategory = Category()
            newCategory.name = etCategory.text.toString()

            db.collection("categorias").add(newCategory).addOnCompleteListener {
                finish()
            }
        }
    }
}
