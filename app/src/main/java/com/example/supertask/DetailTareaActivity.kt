package com.example.supertask

import android.app.AlertDialog
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.supertask.models.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_detail_task.*
import java.util.*

class DetailTareaActivity : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_tarea)


        val tarea = DataHolder.tareaSeleccionada

        Picasso.get().load(tarea.image).into(detailImgMiTarea)

        detailTvCategory.text = tarea.category
        detailTvTitle.text = tarea.name
        if (tarea.isPriority) {
            detailTvPriority.text = "Prioritario"
        } else {
            detailTvPriority.text = "Con calma"
        }

        detailTvDate.text = formatDate(tarea.date, "dd/MM/YYYY HH:mm")

    }

    fun formatDate(date: Date, formatTarget: String): String? {
        val formatter = SimpleDateFormat(formatTarget, Locale.getDefault())
        return formatter.format(date)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete) {
            deleteTarea(DataHolder.tareaSeleccionada)
        }
        return super.onOptionsItemSelected(item)
    }

    fun deleteTarea(tarea: Task) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("¿Quieres eliminar la tarea?")
        builder.setNegativeButton("No") { _, _ ->
        }

        builder.setPositiveButton("Si") { dialogo, _ ->
            db.collection("users").document(auth.currentUser?.uid.toString()).collection("tareas")
                .document(tarea.idTarea!!)
                .delete()
                .addOnSuccessListener {
                    dialogo.dismiss()
                    finish()
                }
                .addOnFailureListener { e -> Log.w("miapp", "Error deleting document", e) }
        }
        builder.show()
    }
}

