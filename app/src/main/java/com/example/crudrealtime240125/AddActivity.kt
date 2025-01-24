package com.example.crudrealtime240125

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.crudrealtime240125.models.Articulo
import com.example.tarea240125.R
import com.example.tarea240125.databinding.ActivityAddBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddActivity : AppCompatActivity() {

    private var nombre = ""
    private var descripcion = ""
    private var precio = 0f
    private var editando = false
    private var itemArticulo = Articulo()

    private lateinit var binding: ActivityAddBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recogerDatos()
        setListeners()
    }

    private fun setListeners() {
        binding.btCancelar.setOnClickListener {
            finish()
        }
        binding.btAdd.setOnClickListener {
            addItem()
        }
    }

    private fun camposCorrectos(): Boolean {
        nombre = binding.etNombre.text.toString().trim()
        descripcion = binding.etDescripcion.text.toString().trim()
        precio = binding.etPrecio.text.toString().toFloatOrNull() ?: 0f

        if (nombre.length < 3) {
            binding.etNombre.error = "Error, el nombre debe de tener más de 3 caracteres."
            return false
        }

        if (descripcion.length < 3) {
            binding.etDescripcion.error = "Error, la descripción debe de tener más de 3 caracteres."
            return false
        }

        if (precio < 1 || precio > 100) {
            binding.etPrecio.error = "Error, el precio no puede ser menor que 0 ni mayor que 100."
            return false
        }
        return true
    }

    private fun recogerDatos() {
        val datos = intent.extras
        if (datos != null) {
            itemArticulo = datos.getSerializable("ITEM") as Articulo
            editando = true
            binding.tvTitulo.text = "Editando"
            pintarDatos()
        }
    }

    private fun pintarDatos() {
        binding.etDescripcion.setText(itemArticulo.descripcion)
        binding.etNombre.setText(itemArticulo.nombre)
        binding.etPrecio.setText(itemArticulo.precio.toString())
    }

    private fun addItem() {
        if (!camposCorrectos()) return

        val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("tienda")
        val articulo = Articulo(nombre, descripcion, precio)

        if (!editando) {
            database.push().setValue(articulo)
                .addOnSuccessListener {
                    Toast.makeText(this, "Artículo agregado con éxito.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al agregar el artículo.", Toast.LENGTH_SHORT).show()
                }
        } else {
            val nodo = database.orderByChild("nombre").equalTo(articulo.nombre)
            nodo.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val key = snapshot.children.first().key!!
                        database.child(key).setValue(articulo)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this@AddActivity,
                                    "Artículo actualizado.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this@AddActivity,
                                    "Error al actualizar el artículo.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@AddActivity,
                        "Error al verificar: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }
    }

