package com.example.proyectomoviles1muggles.ui.users

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import java.io.File
import java.util.*

private const val REQUEST_IMAGE_CAPTURE = 1
private const val REQUEST_SELECT_IMAGE = 2

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

    private fun verificarPermisosCamara() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            tomarFoto()
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tomarFoto()
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun seleccionarFoto() {
        val opciones = arrayOf("Tomar Foto", "Seleccionar desde Galería", "Cancelar")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Seleccionar una opción")
        builder.setItems(opciones) { dialog, which ->
            when (opciones[which]) {
                "Tomar Foto" -> verificarPermisosCamara()
                "Seleccionar desde Galería" -> abrirGaleria()
                "Cancelar" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun tomarFoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            } else {
                Toast.makeText(this, "No hay una aplicación de cámara disponible", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Este dispositivo no tiene cámara", Toast.LENGTH_SHORT).show()
        }
    }


    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as? Bitmap
                    // Guardar la imagen en un Uri temporal si es necesario
                    fotoUri = guardarImagenTemporal(imageBitmap)
                    tvFotoSeleccionada.text = "Foto tomada"
                }
                REQUEST_SELECT_IMAGE -> {
                    fotoUri = data?.data
                    tvFotoSeleccionada.text = "Foto seleccionada"
                }
            }
        }
    }

    private fun guardarImagenTemporal(bitmap: Bitmap?): Uri? {
        if (bitmap == null) return null
        val file = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
        }
        return Uri.fromFile(file)
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

