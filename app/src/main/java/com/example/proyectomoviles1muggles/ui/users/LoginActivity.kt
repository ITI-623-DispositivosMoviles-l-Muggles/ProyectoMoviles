package com.example.proyectomoviles1muggles.ui.users

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.proyectomoviles1muggles.MainActivity
import com.example.proyectomoviles1muggles.ui.users.SignupActivity
import com.example.proyectomoviles1muggles.R
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.et_email)
        val passwordEditText = findViewById<EditText>(R.id.et_password)
        val loginButton = findViewById<Button>(R.id.btn_login)
        val registerTextView = findViewById<TextView>(R.id.tv_register)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        registerTextView.setOnClickListener {
            // Navegar a la actividad de registro
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Inicio de sesión exitoso
                    val user = auth.currentUser
                    Toast.makeText(this, "Bienvenido ${user?.email}", Toast.LENGTH_SHORT).show()

                    // Navegar a MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // Error en el inicio de sesión
                    Log.e("LoginActivity", "Error: ${task.exception?.message}")
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
            R.id.menu_logout -> {
                // Llamar al método de Firebase para cerrar sesión
                FirebaseAuth.getInstance().signOut()

                // Mostrar un mensaje de confirmación
                Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()

                // Redirigir al usuario a la pantalla de inicio de sesión o a la actividad de inicio
                val intent = Intent(this, LoginActivity::class.java)  // Asegúrate de tener LoginActivity
                startActivity(intent)
                finish()  // Opcionalmente, termina la actividad actual
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}