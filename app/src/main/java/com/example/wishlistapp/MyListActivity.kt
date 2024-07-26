package com.example.wishlistapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wishlistapp.databinding.ActivityMyListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MyListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyListBinding
    private lateinit var planAdapter: PlanAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val plans = mutableListOf<MyPlanData>()

    companion object {
        const val SHARED_PREFS = "MySharedPref"
        const val USER_EMAIL_KEY = "user_email"
        const val PLAN_KEY = "plan_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMyListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        val sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString(USER_EMAIL_KEY, "Email not found")

        planAdapter = PlanAdapter(this, plans,
            onEditClick = { plan ->
                // Handle edit click
                val intent = Intent(this, EditPlanActivity::class.java).apply {
                    putExtra(PLAN_KEY, plan.title)
                }
                startActivity(intent)
            },
            onDeleteClick = { plan ->
                // Handle delete click
                Toast.makeText(this, "Delete ${plan.title}", Toast.LENGTH_SHORT).show()
                // Optionally remove the item from Firebase and update the list
                plan.title?.let { databaseReference.child(it).removeValue() }
            }
        )

        binding.rvPlans.layoutManager = LinearLayoutManager(this)
        binding.rvPlans.adapter = planAdapter

        databaseReference = FirebaseDatabase.getInstance("https://wishlistapp-a2748-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("User Plan")

        fetchPlans(userEmail)

        binding.btnDashboard.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            handleLogout()
        }
    }

    private fun fetchPlans(userEmail: String?) {
        if (userEmail == null) {
            Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show()
            return
        }

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                plans.clear()
                for (planSnapshot in snapshot.children) {
                    val plan = planSnapshot.getValue(MyPlanData::class.java)
                    if (plan?.email == userEmail) {
                        plan?.let { plans.add(it) }
                    }
                }
                planAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MyListActivity, "Failed to load plans", Toast.LENGTH_SHORT).show()
            }
        })
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
