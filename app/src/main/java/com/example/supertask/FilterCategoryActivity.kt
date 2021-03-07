package com.example.supertask

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.supertask.adapters.TareasAdapter
import com.example.supertask.models.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_filter_category.*
import kotlinx.android.synthetic.main.fragment_pending.*

class FilterCategoryActivity : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val TAG = "PendingFragment"

    var allTareas: ArrayList<Task> = arrayListOf()
    lateinit var mAdapter: TareasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter_category)

        val categoria = DataHolder.categoriaSeleccionada
        Log.v("FilterCtegoryActivity", "La categoria es $categoria")

        db.collection("users").document(auth.currentUser?.uid.toString()).collection("tareas")
            .whereEqualTo("categoria", categoria).whereEqualTo("completed", false)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                allTareas.clear()
                for (doc in value!!) {
                    val tarea = doc.toObject(Task::class.java)
                    tarea.idTarea = doc.id
                    allTareas.add(tarea)
                }

                mAdapter.notifyDataSetChanged()

            }

        mAdapter = TareasAdapter(allTareas, { task ->
            checkCompleteTask(task)
        }, { task ->
            DataHolder.tareaSeleccionada = task
            startActivity(Intent(this, DetailTareaActivity::class.java))
        })

        filtroRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        filtroRecyclerView.adapter = mAdapter
    }

    fun checkCompleteTask(task: Task) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("¿Quieres completar la tarea?")
        builder.setNegativeButton("No") { _, _ ->

        }
        builder.setPositiveButton("Si") { dialogo, _ ->
            task.completed = true
            db.collection("users").document(auth.currentUser?.uid.toString())
                .collection("tareas").document(task.idTarea!!).set(task)
                .addOnCompleteListener {
                    dialogo.dismiss()
                }
        }
        builder.show()
    }

}

