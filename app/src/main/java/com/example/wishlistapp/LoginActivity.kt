package com.example.wishlistapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.wishlistapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.loginButton.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()

            if (email.isEmpty()) {
                binding.email.error = "isi email untuk login"
                binding.email.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.password.error = "isi password untuk login"
                binding.password.requestFocus()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.email.error = "format email yang dimasukan tidak sesuai !"
                binding.email.requestFocus()
                return@setOnClickListener
            }
            if (password.length < 8) {
                binding.password.error = "password yang dimasukan minimal 8 karakter"
                binding.password.requestFocus()
                return@setOnClickListener
            }
            loginFirebase(email, password)
        }

        binding.signUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginFirebase(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Autentikasi Berhasil !", Toast.LENGTH_SHORT).show()

                    // Simpan email ke Shared Preferences
                    val sharedPreferences = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("user_email", email)
                    editor.apply()

                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Email atau Password salah, silakan periksa kembali", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
