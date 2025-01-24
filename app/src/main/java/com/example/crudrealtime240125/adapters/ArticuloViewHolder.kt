package com.example.crudrealtime240125.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.crudrealtime240125.models.Articulo
import com.example.tarea240125.databinding.ArticuloLayoutBinding

class ArticuloViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    private val binding = ArticuloLayoutBinding.bind(v)

    fun render(item: Articulo, onBorrar: (Articulo) -> Unit, onEdit: (Articulo) -> Unit) {
        binding.tvNombre.text = item.nombre
        binding.tvDescripcion.text = item.descripcion
        binding.tvArticulo.text = item.precio.toString()

        binding.btBorrar.setOnClickListener {
            onBorrar(item)
        }
        binding.btEditar.setOnClickListener {
            onEdit(item)
        }
    }
}
