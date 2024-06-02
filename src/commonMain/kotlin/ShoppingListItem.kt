import kotlinx.serialization.Serializable

@Serializable
data class User(val login: String)

@Serializable
data class Course(val idName: String, val name: String, val description: String)