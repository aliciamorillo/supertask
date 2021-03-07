package com.example.supertask

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.example.supertask.models.Category
import com.example.supertask.models.Users
import com.example.supertask.views.CompletedFragment
import com.example.supertask.views.PendingFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import kotlinx.android.synthetic.main.activity_add_task.*
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()
    val TAG = "miapp"
    val auth = FirebaseAuth.getInstance()

    val allCategorias: ArrayList<Category> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        cargarCategorias()

        bottom_navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.pending -> {
                    goToFragment(PendingFragment())
                    true
                }
                R.id.completed -> {
                    goToFragment(CompletedFragment())
                    true
                }
                R.id.adios -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                else -> false
            }
        }

        bottom_navigation.selectedItemId = R.id.pending
    }

    fun goToFragment(destino: Fragment) {
        supportFragmentManager.beginTransaction().replace(container.id, destino).commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout) {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else if (item.itemId == R.id.profile) {
            startActivity(Intent(this, ProfileActivity::class.java))
        } else if (item.itemId == R.id.category) {
            startActivity(Intent(this, AddCategory::class.java))
        } else if (item.itemId == R.id.filter) {
            startActivity(Intent(this, AddCategory::class.java))
            openChoices()
        }
        return super.onOptionsItemSelected(item)
    }

    fun cargarCategorias() {
        db.collection("categorias")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val categoria = document.toObject(Category::class.java)
                    allCategorias.add(categoria)
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    fun openChoices() {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle("Elige una categoría")

        val nombresCategoria = allCategorias.map { it.name }.toTypedArray()
        var checkedItem = 0

        builder.setSingleChoiceItems(nombresCategoria, checkedItem) { dialog, idSeleccionado ->
            Log.v("miapp", "Ha seleccionado $idSeleccionado")
            checkedItem = idSeleccionado
        }
        builder.setPositiveButton("Aceptar") { dialog, which ->
            Log.v("miapp", "Ha elegido finalmente ${nombresCategoria[checkedItem]}")

            DataHolder.categoriaSeleccionada = nombresCategoria[checkedItem]!!
            startActivity(Intent(this, FilterCategoryActivity::class.java))
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }
}
