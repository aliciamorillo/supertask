package com.example.supertask.views

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.supertask.R
import com.example.supertask.adapters.CompletadoAdapter
import com.example.supertask.adapters.TareasAdapter
import com.example.supertask.models.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_completed.*
import kotlinx.android.synthetic.main.fragment_pending.*

/**
 * A simple [Fragment] subclass.
 */
class CompletedFragment : Fragment() {

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val TAG = "CompletadoFragment"

    var allTareas: ArrayList<Task> = arrayListOf()
    lateinit var mAdapter: CompletadoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_completed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db.collection("users").document(auth.currentUser?.uid.toString()).collection("tareas")
            .whereEqualTo("completed", true)
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

        mAdapter = CompletadoAdapter(allTareas, { tareaEliminar ->
            deleteTarea(tareaEliminar)
        }, { tarea ->
            uncheckTarea(tarea)
        })

        tareaCompletadaReclyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        tareaCompletadaReclyclerView.adapter = mAdapter
    }

    fun deleteTarea(task: Task) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("¿Quieres eliminar la tarea?")
        builder.setNegativeButton("No") { _, _ ->

        }
        builder.setPositiveButton("Si") { dialogo, _ ->
            db.collection("users").document(auth.currentUser?.uid.toString()).collection("tareas")
                .document(task.idTarea!!).delete()
                .addOnSuccessListener {
                    dialogo.dismiss()
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error", e) }
        }
        builder.show()
    }

    fun uncheckTarea(task: Task) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("¿Quieres reactivar la tarea?")
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


