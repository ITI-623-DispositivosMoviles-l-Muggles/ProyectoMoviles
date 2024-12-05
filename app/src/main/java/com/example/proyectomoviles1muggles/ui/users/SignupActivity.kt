package com.example.proyectomoviles1muggles.ui.users

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.proyectomoviles1muggles.MainActivity
import com.example.proyectomoviles1muggles.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class SignupActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etAnioEscolar: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var btnSeleccionarFoto: Button
    private lateinit var btnRegistrar: Button
    private lateinit var tvFotoSeleccionada: TextView

    private var fotoUri: Uri? = null
    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        etNombre = findViewById(R.id.etNombre)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etAnioEscolar = findViewById(R.id.etAnioEscolar)
        etDescripcion = findViewById(R.id.etDescripcion)
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        tvFotoSeleccionada = findViewById(R.id.tvFotoSeleccionada)

        btnSeleccionarFoto.setOnClickListener {
            seleccionarFoto()
        }

        btnRegistrar.setOnClickListener {
            registrarUsuario()
        }
    }

    private fun seleccionarFoto() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            fotoUri = data?.data
            tvFotoSeleccionada.text = "Foto seleccionada"
        }
    }

    private fun registrarUsuario() {
        val nombre = etNombre.text.toString()
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()
        val anioEscolar = etAnioEscolar.text.toString()
        val descripcion = etDescripcion.text.toString()

        if (fotoUri == null || nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid ?: ""
                    val storageRef = storage.reference.child("usuarios/$userId/foto.jpg")

                    storageRef.putFile(fotoUri!!).addOnSuccessListener { uploadTask ->
                        storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            val usuario = hashMapOf(
                                "nombre" to nombre,
                                "email" to email,
                                "anioEscolar" to anioEscolar,
                                "descripcion" to descripcion,
                                "foto" to downloadUrl.toString()
                            )
                            firestore.collection("datosUsuarios").document(userId).set(usuario)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Usuario registrado con éxito", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_main -> {
                // Acción para el menú de configuración
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_login -> {
                // Ir a la actividad de Login
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_register -> {
                // Ir a la actividad de Registro
                val intent = Intent(this, SignupActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

