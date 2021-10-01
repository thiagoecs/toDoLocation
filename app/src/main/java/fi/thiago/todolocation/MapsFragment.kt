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
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Marker

import com.google.android.gms.maps.model.Circle

import android.app.ProgressDialog
import android.location.Location


class MapsFragment :  SupportMapFragment(), OnMapReadyCallback {

/*
    val radiusInMeters = 50.0
    val strokeColor = -0x10000 //red outline
    val shadeColor = 0x44ff0000 //opaque red fill
    val circleOptions =
        CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor)
            .strokeColor(strokeColor).strokeWidth(8f)

 */


    //private var mCircle = mGoogleMap?.addCircle(circleOptions)!!
    private var mMarker: Marker? = null
    private var isNotified = false
    private var mCircle: Circle? = null


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
        val mLatitude = 60.179225
        val mLongitude = 24.830193




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




/*
                val distance = FloatArray(2)
                mCircle?.center?.let {
                    Location.distanceBetween(fusedLocationClient.lastLocation.result.latitude
                        , fusedLocationClient.lastLocation.result.longitude,
                        it.latitude, mCircle!!.center.longitude, distance
                    )
                }
                if(mCircle != null) {
                    if (distance[0] < mCircle?.radius!! && !isNotified) {
                        Toast.makeText(
                            activity,
                            "Inside 1, distance from center: " + distance[0] + " radius: " + (mCircle?.radius
                                    ),
                            Toast.LENGTH_LONG
                        ).show()
                        isNotified = true
                    } else {
                        isNotified = false
                    }
                }

 */





                val currentLoc = LatLng(it.latitude, it.longitude)
                mGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 15f))
            }
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {


/*
                    val distance = FloatArray(2)
                    mCircle?.center?.let {
                        Location.distanceBetween(locationResult.lastLocation.latitude
                            , locationResult.lastLocation.longitude,
                            it.latitude, mCircle!!.center.longitude, distance
                        )
                    }
                    if(mCircle != null) {
                        if (distance[0] < mCircle?.radius!! && !isNotified) {
                            Toast.makeText(
                                activity,
                                "Inside 2, distance from center: " + distance[0] + " radius: " + (mCircle?.radius
                                        ),
                                Toast.LENGTH_LONG
                            ).show()
                            isNotified = true
                        } else {
                            isNotified = false
                        }
                    }

 */
                    Toast.makeText(
                        activity,
                        "Am I moving?",
                        Toast.LENGTH_LONG
                    ).show()




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



        //override fun onMyLocationChange

        googleMap.setOnMyLocationChangeListener { location ->

            val distance = FloatArray(2)

                 /*
                 Location.distanceBetween( mMarker.getPosition().latitude, mMarker.getPosition().longitude,
                 mCircle.getCenter().latitude, mCircle.getCenter().longitude, distance);
                 */

            mCircle?.center?.let {
                Location.distanceBetween(location.latitude, location.longitude,
                    it.latitude, mCircle!!.center.longitude, distance
                )
            }
/*
            Toast.makeText(
                activity,
                "Am I moving?",
                Toast.LENGTH_LONG
            ).show()

 */


/*
            if (distance[0] > mCircle?.radius!!) {
                Toast.makeText(
                    activity,
                    "Outside, distance from center: " + distance[0] + " radius: " + (mCircle?.radius
                        ),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    activity,
                    "Inside, distance from center: " + distance[0] + " radius: " + (mCircle?.radius
                        ),
                    Toast.LENGTH_LONG
                ).show()
            }


 */

            if(mCircle != null) {
                if (distance[0] < mCircle?.radius!! && isNotified == false) {
                    Toast.makeText(
                        activity,
                        "Inside 3, distance from center: " + distance[0] + " radius: " + (mCircle?.radius
                                ),
                        Toast.LENGTH_LONG
                    ).show()
                    isNotified = true
                } else if (distance[0] > mCircle?.radius!!){
                    isNotified = false
                }
            }



        }


        //val latLng = LatLng(mLatitude, mLongitude)
        //drawMarkerWithCircle(latLng)

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


}