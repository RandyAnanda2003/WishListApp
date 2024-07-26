package com.example.wishlistapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.wishlistapp.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    companion object {
        const val SHARED_PREFS = "MySharedPref"
        const val USER_EMAIL_KEY = "user_email"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()


        val sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString(USER_EMAIL_KEY, "Email not found")

        if (userEmail != null) {
            fetchUserProfile(userEmail)
        }



        databaseReference = FirebaseDatabase.getInstance("https://wishlistapp-a2748-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("User Plan")

        fetchPlanCount(userEmail)

        binding.profileButton.setOnClickListener {
            startActivity(Intent(this, MyProfileActivity::class.java))
        }

        binding.newPlanButton.setOnClickListener {
            startActivity(Intent(this, MyPlanActivity::class.java))
        }

        binding.viewPlansButton.setOnClickListener {
            startActivity(Intent(this, MyListActivity::class.java))
        }

        binding.logoutButton.setOnClickListener {
            handleLogout()
        }
    }

    private fun fetchPlanCount(userEmail: String?) {
        if (userEmail == null) {
            Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show()
            return
        }

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var planCount = 0
                for (planSnapshot in snapshot.children) {
                    val plan = planSnapshot.getValue(MyPlanData::class.java)
                    if (plan?.email == userEmail) {
                        planCount++
                    }
                }
                updatePlanCount(planCount)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DashboardActivity, "Failed to load plans", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchUserProfile(email: String) {
        val uid = auth.currentUser?.uid ?: return
        databaseReference = FirebaseDatabase.getInstance().getReference("User Profil").child(uid)

        databaseReference.get().addOnSuccessListener {
            val profileData = it.getValue(MyProfilData::class.java)
            if (profileData != null) {
                binding.userEmail.text = profileData.username
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal memuat data profil", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePlanCount(count: Int) {
        val planCountTextView = findViewById<TextView>(R.id.plan_count_text_view) // Update with the actual ID of your TextView
        planCountTextView.text = count.toString()
    }
    private fun handleLogout() {
        auth.signOut()

        val sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(USER_EMAIL_KEY)
        editor.apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
