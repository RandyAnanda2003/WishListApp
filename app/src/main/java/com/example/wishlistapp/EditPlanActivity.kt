package com.example.wishlistapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wishlistapp.databinding.ActivityEditPlanBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditPlanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditPlanBinding
    private lateinit var databaseReference: DatabaseReference
    private var plan: MyPlanData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val planTitle = intent.getStringExtra(MyListActivity.PLAN_KEY)

        databaseReference = FirebaseDatabase.getInstance("https://wishlistapp-a2748-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("User Plan")

        if (planTitle != null) {
            fetchPlanDetails(planTitle)
        }

        binding.btnSave.setOnClickListener {
            updatePlan(planTitle)
        }
    }

    private fun fetchPlanDetails(planTitle: String) {
        databaseReference.child(planTitle).get().addOnSuccessListener {
            plan = it.getValue(MyPlanData::class.java)
            if (plan != null) {
                binding.etTitle.setText(plan?.title)
                binding.etDate.setText(plan?.tanggal)
                binding.etBudget.setText(plan?.budget)
                binding.tvFullAddress.setText(plan?.lokasi)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load plan details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePlan(originalTitle: String?) {
        if (originalTitle == null || plan == null) {
            Toast.makeText(this, "Plan details are missing", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedTitle = binding.etTitle.text.toString()
        val updatedDate = binding.etDate.text.toString()
        val updatedBudget = binding.etBudget.text.toString()
        val updatedLocation = binding.tvFullAddress.text.toString()

        val updatedPlan = MyPlanData(
            email = plan?.email,
            title = updatedTitle,
            tanggal = updatedDate,
            budget = updatedBudget,
            lokasi = updatedLocation
        )

        databaseReference.child(originalTitle).removeValue().addOnSuccessListener {
            databaseReference.child(updatedTitle).setValue(updatedPlan).addOnSuccessListener {
                Toast.makeText(this, "Rencana berhasil diupdate", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Gagal mengupdate rencana", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal menghapus rencana lama", Toast.LENGTH_SHORT).show()
        }
    }
}
