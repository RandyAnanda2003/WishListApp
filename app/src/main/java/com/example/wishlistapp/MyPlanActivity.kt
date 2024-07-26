package com.example.wishlistapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.wishlistapp.databinding.ActivityMyPlanBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MyPlanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyPlanBinding
    private lateinit var databaseReference: DatabaseReference

    companion object {
        const val SHARED_PREFS = "MySharedPref"
        const val USER_EMAIL_KEY = "user_email"
        private const val MAPS_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMyPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnGmap.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivityForResult(intent, MAPS_REQUEST_CODE)
        }

        binding.btnSave.setOnClickListener {
            val sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
            val userEmail = sharedPreferences.getString(USER_EMAIL_KEY, "Email not found")

            val title = binding.titleTxt.text.toString()
            val tanggal = binding.tanggalTxt.text.toString()
            val budget = binding.budgetTxt.text.toString()
            val lokasi = binding.alamatTxt.text.toString()

            if (title.isEmpty() || tanggal.isEmpty() || budget.isEmpty() || lokasi.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            databaseReference = FirebaseDatabase.getInstance().getReference("User Plan")
            val myPlanData = MyPlanData(userEmail, title, tanggal, budget, lokasi)
            databaseReference.child(title).setValue(myPlanData).addOnSuccessListener {
                binding.titleTxt.setText("")
                binding.tanggalTxt.setText("")
                binding.budgetTxt.setText("")
                binding.alamatTxt.setText("")

                Toast.makeText(this, "Rencana-mu berhasil disimpan", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish() // Finish current activity
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Simpan data rencana gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MAPS_REQUEST_CODE && resultCode == RESULT_OK) {
            val sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
            val savedLocation = sharedPreferences.getString("saved_location", "")
            binding.alamatTxt.setText(savedLocation)
        }
    }
}
