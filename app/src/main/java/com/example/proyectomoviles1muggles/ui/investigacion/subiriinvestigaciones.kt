package com.example.proyectomoviles1muggles.ui.investigacion

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectomoviles1muggles.R
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.proyectomoviles1muggles.ui.users.LoginActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class subiriinvestigaciones : AppCompatActivity() {
    private lateinit var etTitulo: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etConclusiones: EditText
    private lateinit var etRecomendaciones: EditText
    private lateinit var btnSeleccionarPdf: Button
    private lateinit var btnSeleccionarImagenes: Button
    private lateinit var btnEnviarFormulario: Button
    private lateinit var etArea: EditText
    private lateinit var etCiclo: EditText

    private var pdfUri: Uri? = null
    private var imagenUris: ArrayList<Uri> = arrayListOf()

    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subiriinvestigaciones)

        // Verificar si el usuario está autenticado al ingresar a la actividad
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Si no está autenticado, redirigir a la LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()  // Terminar la actividad actual para evitar que el usuario regrese sin estar autenticado
            return
        }

        // Inicialización de los elementos de la interfaz
        etTitulo = findViewById(R.id.etTitulo)
        etDescripcion = findViewById(R.id.etDescripcion)
        etConclusiones = findViewById(R.id.etConclusiones)
        etRecomendaciones = findViewById(R.id.etRecomendaciones)
        btnSeleccionarPdf = findViewById(R.id.btnSeleccionarPdf)
        btnSeleccionarImagenes = findViewById(R.id.btnSeleccionarImagenes)
        btnEnviarFormulario = findViewById(R.id.btnEnviarFormulario)
        etArea = findViewById(R.id.etArea)
        etCiclo = findViewById(R.id.etCiclo)

        btnSeleccionarPdf.setOnClickListener { seleccionarPdf() }
        btnSeleccionarImagenes.setOnClickListener { seleccionarImagenes() }
        btnEnviarFormulario.setOnClickListener { enviarFormulario() }
    }

    private fun seleccionarPdf() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startActivityForResult(intent, 1)
    }

    private fun seleccionarImagenes() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, 2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                pdfUri = data?.data
            } else if (requestCode == 2) {
                if (data?.clipData != null) {
                    val count = data.clipData?.itemCount ?: 0
                    for (i in 0 until count) {
                        val imageUri = data.clipData?.getItemAt(i)?.uri
                        imageUri?.let { imagenUris.add(it) }
                    }
                } else {
                    data?.data?.let { imagenUris.add(it) }
                }
            }
        }
    }

    private fun enviarFormulario() {
        val titulo = etTitulo.text.toString()
        val descripcion = etDescripcion.text.toString()
        val conclusiones = etConclusiones.text.toString()
        val recomendaciones = etRecomendaciones.text.toString()
        val area = etArea.text.toString()
        val ciclo = etCiclo.text.toString()

        // Validar los campos
        if (titulo.isEmpty() || descripcion.isEmpty() || conclusiones.isEmpty() || recomendaciones.isEmpty() ||
            area.isEmpty() || ciclo.isEmpty() || pdfUri == null || imagenUris.size !in 4..6) {
            Toast.makeText(this, "Por favor completa todos los campos correctamente.", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener el correo del usuario autenticado
        val currentUser = auth.currentUser
        val email = currentUser?.email ?: "Desconocido"

        // Crear el objeto de fecha en formato ISO 8601 (cadena)
        val currentDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date())

        // Subir PDF
        val pdfRef = storage.reference.child("investigaciones/$titulo/documento.pdf")
        pdfUri?.let {
            pdfRef.putFile(it).addOnSuccessListener { taskSnapshot ->
                pdfRef.downloadUrl.addOnSuccessListener { pdfUrl ->
                    // Subir imágenes
                    val imageUrls = HashMap<String, String>()  // Usamos un HashMap para asignar cada imagen con un nombre específico
                    val imageUploadTasks = imagenUris.mapIndexed { index, imageUri ->
                        val imageRef = storage.reference.child("investigaciones/$titulo/Image_${index + 1}.jpg")  // Asignamos nombres específicos
                        imageRef.putFile(imageUri).addOnSuccessListener {
                            imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                                // Asignamos la URL de la imagen al HashMap con la clave correspondiente (Image_1, Image_2, etc.)
                                imageUrls["Imagen_${index + 1}"] = downloadUrl.toString()
                            }
                        }
                    }

                    // Cuando todas las imágenes hayan sido subidas, guardar datos en Firestore
                    Tasks.whenAll(imageUploadTasks).addOnCompleteListener {
                        val investigacion = hashMapOf(
                            "titulo" to titulo,
                            "descripcion" to descripcion,
                            "Conclusion" to conclusiones,
                            "Recomendaciones" to recomendaciones,
                            "area" to area,
                            "ciclo" to ciclo,  // Agregar ciclo
                            "PDF" to pdfUrl.toString(),
                            "Correo" to email,
                            "publicationDate" to currentDate  // Agregar la fecha como cadena en formato ISO 8601
                        )

                        investigacion.putAll(imageUrls)  // Agregamos las URLs de las imágenes al HashMap de la investigación

                        firestore.collection("Pruebas").add(investigacion)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Formulario enviado con éxito", Toast.LENGTH_SHORT).show()
                                finish()  // Termina la actividad actual después de enviar el formulario
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
        }
    }
}

