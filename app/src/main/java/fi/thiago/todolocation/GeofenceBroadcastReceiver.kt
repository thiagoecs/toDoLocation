package fi.thiago.todolocation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        val notificationHelper = NotificationHelper(context)
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event...")
            return
        }
        val geofenceList = geofencingEvent.triggeringGeofences
        for (geofence in geofenceList) {
            Log.d(TAG, "onReceive: " + geofence.requestId)
        }
        //        Location location = geofencingEvent.getTriggeringLocation();
        val transitionType = geofencingEvent.geofenceTransition
        when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show()
                notificationHelper.sendHighPriorityNotification(
                    "GEOFENCE_ENTER", "",
                    MapsFragment::class.java
                )
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                Toast.makeText(context, "GEOFENCE_TRANSITION_DWELL", Toast.LENGTH_SHORT).show()
                notificationHelper.sendHighPriorityNotification(
                    "GEOFENCE_DWELL", "",
                    MapsFragment::class.java
                )
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show()
                notificationHelper.sendHighPriorityNotification(
                    "GEOFENCE_EXIT", "",
                    MapsFragment::class.java
                )
            }
        }
    }

    companion object {
        private const val TAG = "GeofenceBroadcastReceiv"
    }
}