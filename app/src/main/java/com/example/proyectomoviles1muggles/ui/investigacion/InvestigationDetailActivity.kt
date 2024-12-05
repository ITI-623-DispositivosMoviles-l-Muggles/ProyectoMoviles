package com.example.proyectomoviles1muggles.ui.investigacion

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyectomoviles1muggles.R
import com.example.proyectomoviles1muggles.adapters.ImageAdapter
import com.google.firebase.firestore.FirebaseFirestore

class InvestigationDetailActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvArea: TextView
    private lateinit var tvAuthor: TextView
    private lateinit var tvDescription: TextView
    private lateinit var rvImages: RecyclerView
    private lateinit var tvConclusion: TextView
    private lateinit var tvRecommendation: TextView

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

        // Recuperar el título o ID de la investigación pasada en el Intent
        val titulo = intent.getStringExtra("title")

        if (titulo != null) {
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

                    } else {
                        Toast.makeText(this, "No se encontró la investigación.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al cargar los datos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
