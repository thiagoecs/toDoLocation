# ToDoLocation

ToDoLocation is an app that allows user to mark locations on a map and attach to them a list of to do items. 
When the user is close enough to the location, he will receive a notification, reminding him of the list of items that he was supposed to do there.

## Getting Started:

Follow the directions here to generate your API key: https://developers.google.com/maps/documentation/android/start#get-key
Follow instructions on file template_google_maps_api.xml to insert you API key, otherwise the app will not work.

# Bugs

Currently there is a bug where if more than one location is added to the app, the geofence tracking will only work on the latest location.

# Data Persistence:

The app uses firebase firestore to save the locations and the list of items tied to them

# Features

* Tracks user location 
* Creation and deletion of markers
* Adition of items to a previously created list.

# TO DO

Besides fixing the known bugs, there are several features that have not been implemented yet, such as:

* Improvement of the UI as a whole
* Allow deletion of items on a previously created list
* Search and filters for locations
* Snippet with info when clicking a marker on the map
* Ability to see where on the map a location is on the list of locations and on the list of items
* Add bottom navigation bar on the list of items screen
* Draw polyline to ilustrate the shortest distance between the user and a marker
* Allow editing of previsouly stored data

