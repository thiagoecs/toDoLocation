package fi.thiago.todolocation

import android.Manifest
import android.annotation.SuppressLint
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
import android.widget.Toast
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Marker

import com.google.android.gms.maps.model.Circle

import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase


class MapsFragment : SupportMapFragment(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {


    //private var mCircle = mGoogleMap?.addCircle(circleOptions)!!
    private var mMarker: Marker? = null
    private var isNotified = false
    private var mCircle: Circle? = null


    var mGoogleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_LOCATION_PERMISSION = 1

    //////////////////////////////////////////////////////////

    private var geofencingClient: GeofencingClient? = null
    private var geofenceHelper: GeofenceHelper? = null

    private val GEOFENCE_RADIUS = 200f
    private val GEOFENCE_ID = "TODO_GEOFENCE_ID"

    private val FINE_LOCATION_ACCESS_REQUEST_CODE = 10001
    private val FINE_LOCATION_ACCESS_REQUEST_CODE_GEOFENCE = 10003
    private val BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002

    private val db = Firebase.firestore
    private val todoList = db.collection("todoList")
    val todoLocationList = mutableListOf<TodoModel>()
    var currentLoc : LatLng? = null

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    /////////////////////////////////////////////////////////


    /////////////////////////////////////////////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        geofencingClient = LocationServices.getGeofencingClient(context)
        geofenceHelper = GeofenceHelper(context)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getLocationUpdates()
        startLocationUpdates()

        setUpMapIfNeeded()
    }
    /////////////////////////////////////////////////////////////////////////////////



    private fun isPermissionGranted(): Boolean {
        return activity?.let {
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } == PackageManager.PERMISSION_GRANTED
    }



    private fun setUpMapIfNeeded() {
        if (mGoogleMap == null) {
            getMapAsync(this)
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        fineLocPermissions()
        setPoiClick(googleMap)
        googleMap.setOnMyLocationChangeListener { location -> }

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
                    FINE_LOCATION_ACCESS_REQUEST_CODE
                )
            }
        }
    }


    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            val geocoder = Geocoder(activity, Locale.getDefault())
            val address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            val result = address[0].getAddressLine(0)


            val radiusInMeters = 50.0
            val strokeColor = -0x10000 //red outline
            val shadeColor = 0x44ff0000 //opaque red fill
            val circleOptions =
                CircleOptions().center(latLng).radius(radiusInMeters).fillColor(shadeColor)
                    .strokeColor(strokeColor).strokeWidth(8f)
            mCircle = mGoogleMap?.addCircle(circleOptions)!!


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
            //drawMarkerWithCircle(latLng)
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


    private fun drawMarkerWithCircle(position: LatLng) {
        val radiusInMeters = 50.0
        val strokeColor = -0x10000 //red outline
        val shadeColor = 0x44ff0000 //opaque red fill
        val circleOptions =
            CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor)
                .strokeColor(strokeColor).strokeWidth(8f)
        mCircle = mGoogleMap?.addCircle(circleOptions)!!
        val markerOptions = MarkerOptions().position(position)
        mMarker = mGoogleMap?.addMarker(markerOptions)
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////

    private fun enableUserLocation() {

        if (Build.VERSION.SDK_INT >= 29) {
            //We need background permission
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                callApi()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    BACKGROUND_LOCATION_ACCESS_REQUEST_CODE
                )
            }
        }
    }

    private fun handleMapLongClick(latLng: LatLng) {
        mGoogleMap!!.clear()
        addMarker(latLng)
        addCircle(latLng, GEOFENCE_RADIUS)
        addGeofence(latLng, GEOFENCE_RADIUS)
    }


    private fun addGeofence(latLng: LatLng, radius: Float) {
        val geofence = geofenceHelper!!.getGeofence(
            GEOFENCE_ID,
            latLng,
            radius,
            Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL or Geofence.GEOFENCE_TRANSITION_EXIT
        )
        val geofencingRequest = geofenceHelper!!.getGeofencingRequest(geofence)
        val pendingIntent = geofenceHelper!!.getPending_Intent()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            geofencingClient!!.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener {
                    Log.d(
                        "Resultes",
                        "onSuccess: Geofence Added..."
                    )
//                    Toast.makeText(requireContext(),"Geofences added",Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    val errorMessage = geofenceHelper!!.getErrorString(e)
                    Log.d("Resultes", "onFailure: $errorMessage")
                }
        }
    }

    private fun addMarker(latLng: LatLng) {
        val markerOptions = MarkerOptions().position(latLng)
        mGoogleMap!!.addMarker(markerOptions)
    }

    private fun addCircle(latLng: LatLng, radius: Float) {
        val circleOptions = CircleOptions()
        circleOptions.center(latLng)
        circleOptions.radius(radius.toDouble())
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0))
        circleOptions.fillColor(Color.argb(64, 255, 0, 0))
        circleOptions.strokeWidth(4f)
        mGoogleMap!!.addCircle(circleOptions)
    }

    override fun onMapLongClick(latLng: LatLng) {
        if (Build.VERSION.SDK_INT >= 29) {
            //We need background permission
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                handleMapLongClick(latLng)
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                ) {
                    //We show a dialog and ask for permission
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        BACKGROUND_LOCATION_ACCESS_REQUEST_CODE
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        BACKGROUND_LOCATION_ACCESS_REQUEST_CODE
                    )
                }
            }
        } else {
            handleMapLongClick(latLng)
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    private fun callApi(){
        todoList
            .get()
            .addOnSuccessListener {
                todoLocationList.clear()
                for (document in it) {
                    val todoLocList = document.toObject<TodoModel>()
                    todoLocList.id = document.id
                    todoLocationList.add(todoLocList)
                }
                for (i in todoLocationList){
                    val latLng = LatLng(i.lat!!,i.long!!)
                    addMarker(latLng)
                    addCircle(latLng, i.range!!.toFloat())
                    addGeofence(latLng, i.range!!.toFloat())
                }
            }.addOnFailureListener {
                Log.e("reuslt",it.message.toString())
            }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.size > 0 && permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                currentLoc()
                enableMyLocation()
                enableUserLocation()
            } else {
                //We do not have the permission..
                Toast.makeText(requireContext(),"Permissions Required",Toast.LENGTH_SHORT).show()
            }
        }
        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.size > 0 && permissions[0] == Manifest.permission.ACCESS_BACKGROUND_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callApi()
            } else {
                //We do not have the permission..
                Toast.makeText(
                    requireContext(),
                    "Background location access is neccessary for geofences to trigger...",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun currentLoc(){
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
    }

    private fun fineLocPermissions(){
        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } == PackageManager.PERMISSION_GRANTED
        ) {
            currentLoc()
            enableMyLocation()
            enableUserLocation()
        } else {
            //Ask for permission
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                FINE_LOCATION_ACCESS_REQUEST_CODE
            )
        }
    }

    //start location updates
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    private fun getLocationUpdates()
    {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = LocationRequest()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 1000
        locationRequest.smallestDisplacement = 5f // 5 m = 0.005 mile
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //set according to your app function
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                if (locationResult.locations.isNotEmpty()) {
                    // get latest location
                    val location = locationResult.lastLocation
                    // use your location object
                    // get latitude , longitude and other info from this
                }
            }
        }
    }

}