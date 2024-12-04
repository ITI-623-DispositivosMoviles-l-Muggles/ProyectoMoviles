package com.example.proyectomoviles1muggles.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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
        holder.downloadButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(investigation.pdf))
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = investigations.size
}
