package com.example.proyectomoviles1muggles.ui.investigacion

import android.os.Bundle
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectomoviles1muggles.R
import com.example.proyectomoviles1muggles.adapters.ImageAdapter
import com.example.proyectomoviles1muggles.adapters.ComentarioAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Button
import com.example.proyectomoviles1muggles.clase.Comment
import com.google.firebase.firestore.Query

class InvestigationDetailActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvArea: TextView
    private lateinit var tvAuthor: TextView
    private lateinit var tvDescription: TextView
    private lateinit var rvImages: RecyclerView
    private lateinit var tvConclusion: TextView
    private lateinit var tvRecommendation: TextView
    private lateinit var btnAgregarComentario: Button  // Inicializamos el botón
    private lateinit var rvComentarios: RecyclerView  // RecyclerView para los comentarios

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_investigation_detail)

        // Vincular vistas
        tvTitle = findViewById(R.id.tvTitle)
        tvArea = findViewById(R.id.tvArea)
        tvAuthor = findViewById(R.id.tvAuthor)
        tvDescription = findViewById(R.id.tvDescription)
        rvImages = findViewById(R.id.rvImages)
        tvConclusion = findViewById(R.id.tvConclusion)
        tvRecommendation = findViewById(R.id.tvRecommendation)
        btnAgregarComentario = findViewById(R.id.btnAgregarComentario)  // Inicializar el botón
        rvComentarios = findViewById(R.id.rvComentarios)  // Inicializar el RecyclerView de comentarios

        // Recuperar el título o ID de la investigación pasada en el Intent
        val titulo = intent.getStringExtra("title") ?: ""

        if (titulo.isNotEmpty()) {
            // Recuperar datos desde Firestore
            firestore.collection("Pruebas")
                .whereEqualTo("titulo", titulo)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val document = documents.documents[0]

                        // Obtener los datos del documento
                        val description = document.getString("descripcion") ?: "Sin descripción"
                        val conclusion = document.getString("Conclusion") ?: "Sin conclusión"
                        val recommendation = document.getString("Recomendaciones") ?: "Sin recomendaciones"
                        val area = document.getString("area") ?: "Sin área"
                        val email = document.getString("Correo") ?: "Desconocido"
                        val pdfUrl = document.getString("PDF") ?: ""

                        // Cargar los datos en los TextViews
                        tvTitle.text = titulo
                        tvArea.text = area
                        tvAuthor.text = email
                        tvDescription.text = description
                        tvConclusion.text = conclusion
                        tvRecommendation.text = recommendation

                        // Recuperar las URLs de las imágenes
                        val imageUrls = mutableListOf<String>()
                        for (i in 1..6) {
                            val imageUrl = document.getString("Imagen_$i")
                            if (!imageUrl.isNullOrEmpty()) {
                                imageUrls.add(imageUrl)
                            }
                        }

                        // Configurar el RecyclerView para mostrar las imágenes
                        val imagesAdapter = ImageAdapter(imageUrls)
                        rvImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                        rvImages.adapter = imagesAdapter

                        // Cargar comentarios
                        loadComments(titulo)

                    } else {
                        Toast.makeText(this, "No se encontró la investigación.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al cargar los datos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Configurar el botón para abrir el diálogo de comentarios
        btnAgregarComentario.setOnClickListener {
            showCommentDialog(titulo)
        }

        // Botones para filtrar los comentarios
        val btnFechaReciente: Button = findViewById(R.id.btnFechaReciente)
        val btnFechaAntigua: Button = findViewById(R.id.btnFechaAntigua)
        val btnRankingAlto: Button = findViewById(R.id.btnRankingAlto)
        val btnRankingBajo: Button = findViewById(R.id.btnRankingBajo)

        btnFechaReciente.setOnClickListener {
            loadComments(titulo, "fecha", true)  // Cargar comentarios más recientes
        }

        btnFechaAntigua.setOnClickListener {
            loadComments(titulo, "fecha", false)  // Cargar comentarios más antiguos
        }

        btnRankingAlto.setOnClickListener {
            loadComments(titulo, "calificacion", true)  // Cargar comentarios con mejor calificación
        }

        btnRankingBajo.setOnClickListener {
            loadComments(titulo, "calificacion", false)  // Cargar comentarios con peor calificación
        }
    }

    // Función para mostrar el diálogo de comentario
    private fun showCommentDialog(titulo: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userEmail = currentUser?.email ?: ""

        // Crear el diálogo
        val dialogView = layoutInflater.inflate(R.layout.dialog_comentario, null)

        val etNombre: EditText = dialogView.findViewById(R.id.etNombre)
        val etComentario: EditText = dialogView.findViewById(R.id.etComentario)
        val ratingBar: RatingBar = dialogView.findViewById(R.id.ratingBar)

        if (userEmail.isNotEmpty()) {
            etNombre.setText(userEmail)
            etNombre.isEnabled = false
        }

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Agregar Comentario")
            .setView(dialogView)
            .setPositiveButton("Guardar") { dialog, _ ->
                val nombre = etNombre.text.toString().trim()
                val comentario = etComentario.text.toString().trim()
                val rating = ratingBar.rating

                if (nombre.isEmpty() || comentario.isEmpty()) {
                    Toast.makeText(this, "Por favor completa todos los campos.", Toast.LENGTH_SHORT).show()
                } else {
                    saveComment(titulo, nombre, comentario, rating)
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

    // Función para guardar el comentario y la calificación en Firestore
    private fun saveComment(titulo: String, nombre: String, comentario: String, rating: Float) {
        val commentData = hashMapOf(
            "nombre" to nombre,
            "comentario" to comentario,
            "calificacion" to rating,
            "fecha" to FieldValue.serverTimestamp()
        )

        // Guardamos el comentario en Firestore bajo la investigación correspondiente
        firestore.collection("Pruebas")
            .document(titulo)  // Suponemos que el título es único
            .collection("comentarios")
            .add(commentData)
            .addOnSuccessListener {
                Toast.makeText(this, "Comentario guardado exitosamente.", Toast.LENGTH_SHORT).show()
                loadComments(titulo) // Recargar los comentarios después de guardar uno nuevo
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar el comentario: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Función para cargar los comentarios desde Firestore con filtros
    // Función para cargar los comentarios desde Firestore con filtros
    private fun loadComments(titulo: String, orderBy: String = "fecha", descending: Boolean = true) {
        val query = firestore.collection("Pruebas")
            .document(titulo)
            .collection("comentarios")
            .apply {
                if (orderBy == "fecha") {
                    // Ordenar por fecha
                    orderBy("fecha", if (descending) Query.Direction.DESCENDING else Query.Direction.ASCENDING)
                } else {
                    // Ordenar por calificación
                    orderBy("calificacion", if (descending) Query.Direction.DESCENDING else Query.Direction.ASCENDING)
                }
            }

        query.get()
            .addOnSuccessListener { documents ->
                val commentList = mutableListOf<Comment>()
                for (document in documents) {
                    val nombre = document.getString("nombre") ?: "Desconocido"
                    val comentario = document.getString("comentario") ?: "Sin comentario"
                    val calificacion = document.getDouble("calificacion")?.toFloat() ?: 0f
                    commentList.add(Comment(nombre, comentario, calificacion))
                }

                // Configurar el RecyclerView para los comentarios
                val comentarioAdapter = ComentarioAdapter(commentList)
                rvComentarios.layoutManager = LinearLayoutManager(this)
                rvComentarios.adapter = comentarioAdapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar comentarios: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
