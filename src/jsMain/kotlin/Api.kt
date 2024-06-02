import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

val jsonClient = HttpClient {
    install(ContentNegotiation) {
        json()
    }
}

suspend fun getCoursesList(): List<Course> {
    return jsonClient.get("/courses").body()
}

suspend fun tryLogin(user: User): HttpResponse {
    return jsonClient.post("/login") {
        contentType(ContentType.Application.Json)
        setBody(user)
    }
}

suspend fun trySignup(user: User): HttpResponse {
    return jsonClient.post("/signup") {
        contentType(ContentType.Application.Json)
        setBody(user)
    }
}

suspend fun getCourseText(user: User, courseId: String): String {
    return jsonClient.get("/courses/${courseId}") {
        contentType(ContentType.Application.Json)
        setBody(user)
    }.body()
}