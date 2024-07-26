package com.example.wishlistapp

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.util.Locale

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var searchBar: AutoCompleteTextView
    private lateinit var saveButton: Button
    private var selectedLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Places API
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)

        // Initialize the map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize the search bar
        searchBar = findViewById(R.id.search_bar)
        searchBar.setOnClickListener {
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
                .build(this)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }

        // Initialize the save button
        saveButton = findViewById(R.id.btn_save_location)
        saveButton.setOnClickListener {
            if (selectedLocation != null) {
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses = geocoder.getFromLocation(selectedLocation!!.latitude, selectedLocation!!.longitude, 1)
                if (addresses != null) {
                    if (addresses.isNotEmpty()) {
                        val address = addresses[0].getAddressLine(0)
                        saveLocationToSharedPreferences(address)
                        Toast.makeText(this, "Location saved: $address", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } else {
                Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Set default location (optional)
        val defaultLocation = LatLng(-34.0, 151.0)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                selectedLocation = place.latLng
                mMap.clear()
                mMap.addMarker(MarkerOptions().position(selectedLocation!!).title(place.name))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation!!, 15f))
                searchBar.setText(place.name)
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = Autocomplete.getStatusFromIntent(data!!)
                Toast.makeText(this, status.statusMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveLocationToSharedPreferences(address: String) {
        val sharedPreferences = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("saved_location", address)
        editor.apply()
    }

    companion object {
        private const val AUTOCOMPLETE_REQUEST_CODE = 1
    }
}
