import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Users: Table() {
    val login = varchar("login", 255)
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(login)
}

object Courses: Table() {
    val idName = varchar("idName", 255)
    val name = varchar("name", 255)
    val description = varchar("description", 255)
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(idName)
}

object Purchases: Table() {
    val userLogin = varchar("userLogin", 255) references Users.login
    val courseId = varchar("courseId", 255) references Courses.idName
}

object CoursesTexts: Table() {
    val courseId = varchar("courseId", 255) references Courses.idName
    val text = varchar("text", 2550)
}

fun main() {
    Database.connect("jdbc:mysql://localhost:3306/chesscources", driver = "com.mysql.cj.jdbc.Driver",
        user = "root", password = "mama228")
    transaction {
        SchemaUtils.create(Users, Courses, Purchases, CoursesTexts)
    }
    embeddedServer(Netty, 9090) {
        val secret = /*environment.config.property("jwt.secret").getString()*/ "secret"
        install(ContentNegotiation) {
            json()
        }
        install(CORS) {
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Delete)
            anyHost()
        }
        install(Compression) {
            gzip()
        }
        install(Authentication) {
            jwt("jwt-auth") {
                verifier(JWT
                    .require(Algorithm.HMAC256(secret))
                    .build())
                validate {
                    if (it.payload.getClaim("login").asString() != null)
                        JWTPrincipal(it.payload)
                    else
                        null
                }
            }
        }
        routing {
            staticResources("/", "static")
            get("/courses") {
                val courses = transaction {
                    Courses.selectAll().map {
                        Course(it[Courses.idName], it[Courses.name], it[Courses.description])
                    }
                }
                call.respond(courses)
            }
            post("/login") {
                val user = call.receive<User>()
                val usersNumber = transaction {
                    Users.select { Users.login eq user.login }.count().toInt()
                }
                if (usersNumber == 0) {
                    call.respond(HttpStatusCode.Unauthorized, "Wrong login")
                    return@post
                }
                val token = JWT.create()
                    .withClaim("login", user.login)
                    .sign(Algorithm.HMAC256(secret))
                call.respond(HttpStatusCode.OK, token)
            }
            post("/signup") {
                val user = call.receive<User>()
                val usersNumber = transaction {
                    Users.select { Users.login eq user.login }.count().toInt()
                }
                if (usersNumber > 0) {
                    call.respond(HttpStatusCode.Unauthorized, "This login is already in use")
                    return@post
                }
                transaction {
                    Users.insert {
                        it[login] = user.login
                    }
                }
                call.respond(HttpStatusCode.OK)
            }
            authenticate("jwt-auth") {
                get("/courses/{courseId}") {
                    val user = call.receive<User>()
                    val courseId = call.parameters["courseId"] ?: error("Invalid get request")
                    var doesUserHaveCourse = false
                    transaction {
                        Purchases.select { Purchases.userLogin eq user.login }.forEach {
                            if (it[Purchases.courseId] == courseId)
                                doesUserHaveCourse = true
                        }
                    }
                    if (!doesUserHaveCourse) {
                        call.respond("")
                        return@get
                    }
                    val courseText = transaction {
                        CoursesTexts.select { CoursesTexts.courseId eq courseId }.map {
                            it[CoursesTexts.text]
                        }.firstOrNull() ?: error("There's no text")
                    }
                    call.respond(courseText)
                }
            }
        }
    }.start(wait = true)
}