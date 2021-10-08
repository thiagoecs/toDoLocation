package fi.thiago.todolocation

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_locations.*


import com.google.android.gms.maps.model.BitmapDescriptorFactory


class AddLocationsActivity : AppCompatActivity(), OnMapReadyCallback {
    var latLong: LatLng? = null
    val MAP_CAMERA_ZOOM = 11f
    private lateinit var itemsData: String
    var marker: Marker? = null
    val hashMap: HashMap<String, Boolean> = HashMap()
    var lat: Double = 0.0
    var long: Double = 0.0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_locations)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        add_item_btn.setOnClickListener {
            itemsData = itemsinput.text.toString()
            hashMap[itemsData] = false
            println("hashMap : $hashMap\n")
            items.append("$itemsData,")
            itemsinput.setText("")
        }
        submit_btn.setOnClickListener {
            val db = Firebase.firestore
            val todo = TodoModel(null, lat, long, title_input.text.toString(), 50.0, hashMap)
            db.collection("todoList").add(todo).addOnSuccessListener {
                Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
                title_input.setText("")
            }.addOnFailureListener {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                Log.e("error", it.message.toString())
            }

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        /*val sydney = LatLng(-33.852, 151.211)
        marker = googleMap.addMarker(
            sydney.let {
                MarkerOptions()
                    .position(it)
            }
        )*/

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (this.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                Log.d(
                    "GEOLOCATION",
                    "last location latitude:${it?.latitude} and longitude: ${it?.longitude}"
                )
                val currentLoc = LatLng(it.latitude, it.longitude)
                marker = googleMap.addMarker(MarkerOptions().position(currentLoc))
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        currentLoc,
                        MAP_CAMERA_ZOOM
                    )
                )
            }
        }



        googleMap.setOnMapClickListener {
            lat = it.latitude
            long = it.longitude
            Log.e("result", it.latitude.toString())
            if (marker!!.equals(null)) {
                marker = googleMap.addMarker(
                    MarkerOptions()
                        .position(it)

                )
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, MAP_CAMERA_ZOOM))
            } else {
                marker!!.position = it
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, MAP_CAMERA_ZOOM))
            }
        }
    }


}