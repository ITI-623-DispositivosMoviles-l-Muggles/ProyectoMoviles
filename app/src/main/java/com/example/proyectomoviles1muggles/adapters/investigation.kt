package com.example.proyectomoviles1muggles.adapters

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast
import com.example.proyectomoviles1muggles.R
import com.example.proyectomoviles1muggles.clase.Investigation

class InvestigationAdapter(
    private val investigations: List<Investigation>
) : RecyclerView.Adapter<InvestigationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tv_title)
        val area: TextView = view.findViewById(R.id.tv_area)
        val author: TextView = view.findViewById(R.id.tv_author)
        val description: TextView = view.findViewById(R.id.tv_description)
        val downloadButton: Button = view.findViewById(R.id.btn_download_pdf)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_investigation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val investigation = investigations[position]
        holder.title.text = investigation.titulo
        holder.area.text = investigation.area
        holder.author.text = investigation.Correo
        holder.description.text = investigation.descripcion

        // Establecer el evento del botón para descargar el PDF
        holder.downloadButton.setOnClickListener {
            // Verifica si el enlace de PDF está disponible
            if (investigation.PDF.isNotEmpty()) {
                downloadPdf(holder.itemView.context, investigation.PDF)
            } else {
                Toast.makeText(holder.itemView.context, "PDF no disponible", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = investigations.size

    // Función para descargar el PDF utilizando DownloadManager
    private fun downloadPdf(context: Context, pdfUrl: String) {
        // Crear el objeto DownloadManager
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // Crear la URI del archivo PDF
        val uri = Uri.parse(pdfUrl)

        // Crear una solicitud de descarga
        val request = DownloadManager.Request(uri)
            .setTitle("Descargando PDF")
            .setDescription("Cargando el archivo PDF de la investigación.")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Investigación_${System.currentTimeMillis()}.pdf") // Establecer nombre del archivo
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) // Notificación de finalización

        // Iniciar la descarga
        downloadManager.enqueue(request)

        // Mostrar un mensaje indicando que la descarga ha comenzado
        Toast.makeText(context, "Descarga iniciada", Toast.LENGTH_SHORT).show()
    }
}
