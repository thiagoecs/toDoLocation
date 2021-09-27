package fi.thiago.todolocation

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class MapsFragment :  SupportMapFragment(), OnMapReadyCallback {


    var mGoogleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val REQUEST_LOCATION_PERMISSION = 1

    private fun isPermissionGranted(): Boolean {
        return activity?.let {
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } == PackageManager.PERMISSION_GRANTED
    }


    override fun onResume() {
        super.onResume()
        setUpMapIfNeeded()
    }

    private fun setUpMapIfNeeded() {
        if (mGoogleMap == null) {
            getMapAsync(this)
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap



        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        if (activity?.let {
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
                mGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 15f))
            }
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    Log.d(
                        "GEOLOCATION",
                        "new location latitude: ${location.latitude} and longitude: ${location.longitude}"
                    )
                }

            }
        }

        setMapLongClick(googleMap)
        setPoiClick(googleMap)
        enableMyLocation()

    }

    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            try {
                mGoogleMap?.isMyLocationEnabled = true
            } catch (e: SecurityException) {
                Log.e("map", "cannot locate ${e.localizedMessage}")
            }

        } else {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }



    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            val geocoder = Geocoder(activity ,Locale.getDefault())
            val address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            val result = address[0].getAddressLine(0)


            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude,
                address
            )
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(result)
                    .snippet(snippet)
            )
        }
    }


    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()
        }
    }
}