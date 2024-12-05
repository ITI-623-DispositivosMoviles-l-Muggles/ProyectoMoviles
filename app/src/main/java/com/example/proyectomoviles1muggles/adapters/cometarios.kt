package com.example.proyectomoviles1muggles.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectomoviles1muggles.R
import com.example.proyectomoviles1muggles.clase.Comment

class ComentarioAdapter(private var commentList: MutableList<Comment>) : RecyclerView.Adapter<ComentarioAdapter.ComentarioViewHolder>() {

    // Método para actualizar los comentarios
    fun actualizarComentarios(nuevosComentarios: List<Comment>) {
        this.commentList.clear()
        this.commentList.addAll(nuevosComentarios)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComentarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comentario, parent, false)
        return ComentarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComentarioViewHolder, position: Int) {
        val comment = commentList[position]
        holder.tvNombre.text = comment.nombre
        holder.tvComentario.text = comment.comentario
        holder.ratingBar.rating = comment.calificacion
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    inner class ComentarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvComentario: TextView = itemView.findViewById(R.id.tvComentario)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
    }
}
