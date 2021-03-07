package com.example.supertask.views

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.supertask.AddTask
import com.example.supertask.DataHolder
import com.example.supertask.DetailTareaActivity

import com.example.supertask.R
import com.example.supertask.adapters.TareasAdapter
import com.example.supertask.models.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_pending.*

/**
 * A simple [Fragment] subclass.
 */
class PendingFragment : Fragment() {

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val TAG = "PendingFragment"

    var allTareas: ArrayList<Task> = arrayListOf()
    var allTareasBusqueda: ArrayList<Task> = arrayListOf()

    lateinit var mAdapter: TareasAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pending, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        floating_action_button.setOnClickListener {
            startActivity(Intent(context, AddTask::class.java))
        }

        db.collection("users").document(auth.currentUser?.uid.toString()).collection("tareas")
            .whereEqualTo("completed", false)
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
                allTareasBusqueda.clear()
                allTareasBusqueda.addAll(allTareas)

                mAdapter.notifyDataSetChanged()

            }

        mAdapter = TareasAdapter(allTareasBusqueda, { task ->
            checkCompleteTask(task)
        }, { task ->
            DataHolder.tareaSeleccionada = task
            startActivity(Intent(context, DetailTareaActivity::class.java))
        })

        taskRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        taskRecyclerView.adapter = mAdapter

        busqueda()
    }

    fun busqueda() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {

                val result = allTareas.filter { it.name!!.contains(newText, true) || it.category!!.contains(newText, true) }

                allTareasBusqueda.clear()
                allTareasBusqueda.addAll(result)
                mAdapter.notifyDataSetChanged()

                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }
        })
    }

    fun checkCompleteTask(task: Task) {
        val builder = AlertDialog.Builder(context)
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


