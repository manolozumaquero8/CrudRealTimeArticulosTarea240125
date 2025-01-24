package com.example.crudrealtime240125

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tarea240125.R
import com.example.tarea240125.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private val responsableLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val datos = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                try {
                    val cuenta = datos.getResult(ApiException::class.java)
                    val idToken = cuenta?.idToken
                    if (cuenta != null && idToken != null) {
                        val credenciales = GoogleAuthProvider.getCredential(idToken, null)
                        FirebaseAuth.getInstance().signInWithCredential(credenciales)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    irActivityPrincipal()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Error al autenticar con Google.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }
                } catch (e: ApiException) {
                    Toast.makeText(
                        this,
                        "Fallo en la autenticación: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            if (it.resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "El usuario a cancelado.", Toast.LENGTH_SHORT).show()
            }
        }

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = Firebase.auth
        setListeners()
    }
    private fun setListeners() {
        binding.btLogin.setOnClickListener {
            loginGoogle()
        }
    }

    private fun loginGoogle() {
        val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()
        val googleClient = GoogleSignIn.getClient(this, googleConf)

        googleClient.signOut()

        responsableLauncher.launch(googleClient.signInIntent)
    }

    private fun irActivityPrincipal() {
        startActivity(Intent(this, PrincipalActivity::class.java))
    }
    override fun onStart() {
        super.onStart()
        val usuario = auth.currentUser
        if (usuario != null) irActivityPrincipal()
    }
}