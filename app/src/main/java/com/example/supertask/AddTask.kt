package com.example.supertask

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.example.supertask.models.Category
import com.example.supertask.models.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_add_task.*
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class AddTask : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    val TAG = "AddTask"

    val allCategorias: ArrayList<Category> = arrayListOf()
    var dateSelected: Date = Date()

    // Img
    val CHOOSING_IMAGE_REQUEST = 1234
    var fileUri: Uri? = null
    val storage = FirebaseStorage.getInstance()

    lateinit var imageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        title = "Añadir Tarea"

        cargarCategorias()

        etCategoryName.setOnClickListener {
            openChoices()
        }

        etDateTimeName.setOnClickListener {
            pickDateTime()
        }

        btSaveTask.setOnClickListener {
            uploadFile()
        }

        // img
        imageReference = storage.reference.child("tareas")

        imgTask.setOnClickListener {
            seleccionarImagen()
        }
    }

    fun crearTarea(imgUrl: String) {
        val newTask = Task()
        newTask.name = etTaskName.text.toString()
        newTask.category = etCategoryName.text.toString()
        newTask.date = dateSelected
        newTask.isPriority = checkPriority.isChecked
        newTask.image = imgUrl

        db.collection("users").document(auth.currentUser?.uid.toString()).collection("tareas")
            .add(newTask).addOnCompleteListener {
                finish()
            }
    }

    //Llama a la pantalla para seleccionar la imagen
    fun seleccionarImagen() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_PICK
        startActivityForResult(
            Intent.createChooser(intent, "Selecciona una imagen"),
            CHOOSING_IMAGE_REQUEST
        )
    }

    // Se ejecuta después de seleccionar la foto
    // Esta atento a cuando seleccionas un valor de tu galeria de fotos
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CHOOSING_IMAGE_REQUEST && resultCode == RESULT_OK && data != null
            && data.data != null
        ) {
            fileUri = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, fileUri)
                imgTask.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Obtiene la extensión del fichero
    private fun getFileExtension(uri: Uri): String {
        val contentResolver = contentResolver
        val mime = MimeTypeMap.getSingleton()

        return mime.getExtensionFromMimeType(contentResolver.getType(uri))!!
    }

    // Valida que el fichero tenga un nombre
    private fun validateInputFileName(fileName: String): Boolean {
        if (TextUtils.isEmpty(fileName)) {
            Toast.makeText(this, "La foto necesita un nombre", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun uploadFile() {
        if (fileUri != null) {
            val fileName = "img-" + Date().time

            if (!validateInputFileName(fileName)) {
                return
            }

            val fileRef = imageReference.child(fileName + "." + getFileExtension(fileUri!!))
            fileRef.putFile(fileUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        Log.e(TAG, "Uri: $uri")

                        // Creamos nuestra tarea en firebase
                        crearTarea(uri.toString())
                        Toast.makeText(this, "Fichero subido", Toast.LENGTH_SHORT).show()

                    }
                }
                .addOnFailureListener { exception ->
                    // Mostramos mensaje en caso de fallo
                    Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                }

        } else {
            Toast.makeText(this, "No hay fichero", Toast.LENGTH_SHORT).show()
        }
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

            etCategoryName.setText(nombresCategoria[checkedItem])
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    fun pickDateTime() {
        val currentDateTime = Calendar.getInstance()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
        val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val startMinute = currentDateTime.get(Calendar.MINUTE)

        DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, day ->

            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                Log.i("miapp", "Lo tengo todo!")

                val pickedDateTime = Calendar.getInstance()
                pickedDateTime.set(year, month, day, hour, minute)

                val fechaAPintar = formatDate(pickedDateTime.time, "dd/MM/YYYY HH:mm")
                etDateTimeName.setText(fechaAPintar)

                dateSelected = pickedDateTime.time

            }, startHour, startMinute, true).show()

        }, startYear, startMonth, startDay).show()
    }

    fun formatDate(date: Date, formatTarget: String): String? {
        val formatter = SimpleDateFormat(formatTarget, Locale.getDefault())
        return formatter.format(date)
    }

}
