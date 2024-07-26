package com.example.wishlistapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wishlistapp.databinding.ActivityMyProfileBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MyProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyProfileBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    companion object {
        const val SHARED_PREFS = "MySharedPref"
        const val USER_EMAIL_KEY = "user_email"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString(USER_EMAIL_KEY, null)

        if (userEmail != null) {
            fetchUserProfile(userEmail)
        }

        binding.btnUpdateProfile.setOnClickListener {
            updateProfile()
        }

        binding.btnDashboard.setOnClickListener {
            startActivity(Intent(this,DashboardActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            handleLogout()
        }
    }

    private fun fetchUserProfile(email: String) {
        val uid = auth.currentUser?.uid ?: return
        databaseReference = FirebaseDatabase.getInstance().getReference("User Profil").child(uid)

        databaseReference.get().addOnSuccessListener {
            val profileData = it.getValue(MyProfilData::class.java)
            if (profileData != null) {
                binding.username.setText(profileData.username)
                binding.email.setText(email)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal memuat data profil", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfile() {
        val uid = auth.currentUser?.uid ?: return
        val newUsername = binding.username.text.toString()
        val newEmail = binding.email.text.toString()
        val oldPassword = binding.etOldPassword.text.toString()
        val newPassword = binding.etNewPassword.text.toString()

        if (newUsername.isEmpty() || newEmail.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        // Update username
        databaseReference = FirebaseDatabase.getInstance().getReference("User Profil").child(uid)
        databaseReference.child("username").setValue(newUsername).addOnSuccessListener {
            Toast.makeText(this, "Username berhasil diperbarui", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal memperbarui username", Toast.LENGTH_SHORT).show()
        }

        // Update email and password
        val user = auth.currentUser
        if (user != null && user.email != null) {
            // Re-authenticate the user
            val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
            user.reauthenticate(credential).addOnCompleteListener {
                if (it.isSuccessful) {
                    // Update email
                    user.updateEmail(newEmail).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Email berhasil diperbarui", Toast.LENGTH_SHORT).show()
                            val sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString(USER_EMAIL_KEY, newEmail)
                            editor.apply()
                        } else {
                            Toast.makeText(this, "Gagal memperbarui email", Toast.LENGTH_SHORT).show()
                        }
                    }

                    // Update password
                    user.updatePassword(newPassword).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Gagal memperbarui password", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Gagal mengautentikasi ulang", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleLogout() {
        auth.signOut()

        val sharedPreferences = getSharedPreferences(DashboardActivity.SHARED_PREFS, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(DashboardActivity.USER_EMAIL_KEY)
        editor.apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
