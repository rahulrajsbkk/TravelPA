package `in`.ouon.travelpa.model

data class User(
    val avatar: String? = null,
    val dob: String? = null,
    val email: String? = null,
    val location: String? = null,
    val name: String? = null,
    val phone: String? = null,
    var userId: String? = null
)