package com.example.crudrealtime240125

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.crudrealtime240125.adapters.ArticuloAdapter
import com.example.crudrealtime240125.models.Articulo
import com.example.crudrealtime240125.providers.ArticuloProvider
import com.example.tarea240125.R
import com.example.tarea240125.databinding.ActivityPrincipalBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class PrincipalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrincipalBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    var adapter = ArticuloAdapter(mutableListOf(), { item -> borrarItem(item) }, { item -> editarItem(item) })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().getReference("tienda")

        setRecycler()
        setListeners()
        setMenuLateral()
    }

    private fun setMenuLateral() {
        binding.navegation.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item_logout -> {
                    auth.signOut()
                    finish()
                    true
                }
                R.id.item_salir -> {
                    finishAffinity()
                    true
                }
                R.id.item_borrar -> {
                    borrarTodo()
                    true
                }
                else -> true
            }
        }
    }

    private fun borrarTodo() {
        AlertDialog.Builder(this)
            .setTitle("Confirmación")
            .setMessage("¿Estás seguro de que deseas borrar todos los artículos?")
            .setPositiveButton("Sí") { _, _ ->
                database.removeValue().addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Todos los artículos eliminados.", Toast.LENGTH_SHORT).show()
                        recuperarDatosArticulos()
                    } else {
                        Toast.makeText(this, "No se pudo eliminar.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun setRecycler() {
        val layoutManager = LinearLayoutManager(this)
        binding.recarticulo.layoutManager = layoutManager
        binding.recarticulo.adapter = adapter
        recuperarDatosArticulos()
    }

    private fun recuperarDatosArticulos() {
        val articuloProvider = ArticuloProvider()
        articuloProvider.getDatos { listaArticulos ->
            binding.imageView.visibility = if (listaArticulos.isEmpty()) View.VISIBLE else View.INVISIBLE
            adapter.lista = listaArticulos
            adapter.notifyDataSetChanged()
        }
    }

    private fun setListeners() {
        binding.floatingActionButton.setOnClickListener {
            irActivityAdd()
        }
    }

    private fun irActivityAdd(bundle: Bundle? = null) {
        val intent = Intent(this, AddActivity::class.java)
        bundle?.let { intent.putExtras(it) }
        startActivity(intent)
    }

    private fun borrarItem(item: Articulo) {
        database.orderByChild("nombre").equalTo(item.nombre)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        snapshot.children.first().ref.removeValue()
                            .addOnSuccessListener {
                                adapter.lista.remove(item)
                                adapter.notifyDataSetChanged()
                                Toast.makeText(this@PrincipalActivity, "Artículo eliminado.", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@PrincipalActivity, "Error al borrar el artículo.", Toast.LENGTH_SHORT).show()
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun editarItem(item: Articulo) {
        val bundle = Bundle().apply { putSerializable("ITEM", item) }
        irActivityAdd(bundle)
    }

    override fun onResume() {
        super.onResume()
        recuperarDatosArticulos()
    }
}
