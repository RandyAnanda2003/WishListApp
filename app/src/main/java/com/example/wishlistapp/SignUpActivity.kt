package com.example.wishlistapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.wishlistapp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    lateinit var binding : ActivitySignUpBinding
    lateinit var auth : FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        binding.signupButton.setOnClickListener{
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            val username = binding.username.text.toString()

            if (email.isEmpty()) {
                binding.email.error = "Email harus diisi!"
                binding.email.requestFocus()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                binding.email.error = "Email tidak valid!"
                binding.email.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.password.error = "Password harus diisi!"
                binding.password.requestFocus()
                return@setOnClickListener
            }
            if (password.length < 8) {
                binding.password.error = "Password harus berisi minimal 8 karakter"
                binding.password.requestFocus()
                return@setOnClickListener
            }

            if (username.isEmpty()) {
                binding.username.error = "Username harus diisi!"
                binding.username.requestFocus()
                return@setOnClickListener
            }

            RegisterFirebase(email, username, password)
        }

        binding.loginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun RegisterFirebase(email: String, username: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val uid = user.uid
                        saveUserProfile(uid, username)
                    }
                } else {
                    Toast.makeText(this, "${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserProfile(uid: String, username: String) {
        databaseReference = FirebaseDatabase.getInstance().getReference("User Profil")
        val myProfilData = MyProfilData(username)
        databaseReference.child(uid).setValue(myProfilData).addOnSuccessListener {
            Toast.makeText(this, "Pembuatan Akun Berhasil", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal menyimpan data profil ke database", Toast.LENGTH_SHORT).show()
        }
    }
}
