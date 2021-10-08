package fi.thiago.todolocation

data class TodoModel(
    var id : String? = null,
    var lat: Double? = null ,
    var long: Double? = null,
    var title : String? = null,
    var range : Double? = null,
    var hashMap: HashMap<String,Boolean>? = null
)