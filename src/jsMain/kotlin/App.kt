import emotion.react.css
import io.ktor.client.statement.*
import io.ktor.http.*
import js.core.jso
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.form
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.title
import react.dom.html.ReactHTML.ul
import react.router.RouterProvider
import react.router.dom.Link
import react.router.dom.createHashRouter
import react.useEffectOnce
import react.useState
import web.cssom.*
import web.html.InputType

val scope = MainScope()

val CoursesList = FC<Props> {
    var coursesList by useState(emptyList<Course>())
    useEffectOnce {
        scope.launch {
            coursesList = getCoursesList()
        }
    }

    NavigationBar()
    div {
        title {
            +"Courses"
        }
        ul {
            coursesList.forEach { course ->
                li {
                    CoursePanel {
                        idName = course.idName
                        name = course.name
                        description = course.description
                    }
                }
            }
        }
    }
}

val NavigationBar = FC<Props> {
     div {
        Link {
            css {
                paddingLeft = 20.px
            }
            to = "/"
            +"chesscourses.com"
        }
        Link {
            css {
                paddingLeft = 20.px
            }
            to = "/courses"
            +"Courses"
        }
        if (TokenManager.isAuth()) {
            Link {
                css {
                    paddingLeft = 20.px
                }
                to = "/me"
                +"My profile"
            }
        } else {
            Link {
                css {
                    paddingLeft = 20.px
                }
                to = "/login"
                +"Log in"
            }
        }
    }
}

object TokenManager {
    var token = ""
        private set
    var user = ""
        private set
    fun isAuth(): Boolean = token != ""
    fun setState(_token: String, _user: String) {
        if (token == "") {
            token = _token
            user = _user
        }
    }
}

val LoginPage = FC<Props> {
    var current by useState("Enter your login here")
    var message by useState("")

    NavigationBar()
    form {
        p {
            css {
                color = Color("#FF0000")
            }
            +message
        }
        css {
            justifyContent = JustifyContent.center
            alignItems = AlignItems.center
            textAlign = TextAlign.center
            marginTop = 290.px
            marginLeft = 670.px
            marginRight = 670.px
            backgroundColor = Color("#F0D9B5")
            paddingTop = 20.px
            paddingBottom = 20.px
        }
        input {
            type = InputType.text
            value = current
            onChange = {
                current = it.target.value
            }
        }
        input {
            css {
                backgroundColor = Color("#B58863")
            }
            type = InputType.submit
            value = "Log in"
        }
        p {
            Link {
                to = "/signup"
                button {
                    css {
                        backgroundColor = Color("#B58863")
                    }
                    +"Or you can sign up"
                }
            }
        }
        onSubmit = {
            it.preventDefault()
            scope.launch {
                val loginTry = tryLogin(User(current))
                if (loginTry.status == HttpStatusCode.OK) {
                    TokenManager.setState(loginTry.bodyAsText(), current)
                    window.location.href = "http://127.0.0.1:9090/#/courses"
                } else
                    message = "Wrong login"
            }
        }
    }
}

val SignupPage = FC<Props> {
    var current by useState("Enter your new login here")
    var message by useState("")
    NavigationBar()
    form {
        p {
            css {
                color = Color("#FF0000")
            }
            +message
        }
        css {
            justifyContent = JustifyContent.center
            alignItems = AlignItems.center
            textAlign = TextAlign.center
            marginTop = 290.px
            marginLeft = 670.px
            marginRight = 670.px
            backgroundColor = Color("#F0D9B5")
            paddingTop = 20.px
            paddingBottom = 20.px
        }
        input {
            type = InputType.text
            value = current
            onChange = {
                current = it.target.value
            }
        }
        input {
            css {
                backgroundColor = Color("#B58863")
            }
            type = InputType.submit
            value = "Sign up"
        }
        onSubmit = {
            it.preventDefault()
            scope.launch {
                val signupTry = trySignup(User(current))
                if (signupTry.status == HttpStatusCode.OK) {
                    window.location.href = "http://127.0.0.1:9090/#/login"
                } else
                    message = "This login is already in use"
            }
        }
    }
}

val Home = FC<Props> {
    if (TokenManager.isAuth())
        window.location.href = "http://127.0.0.1:9090/#/courses"
    NavigationBar()
    div {
        css {
            justifyContent = JustifyContent.center
            alignItems = AlignItems.center
            textAlign = TextAlign.center
            marginTop = 290.px
            marginLeft = 680.px
            marginRight = 680.px
            backgroundColor = Color("#F0D9B5")
            paddingBottom = 5.px

        }
        p {
            +"Sign up or explore the courses."
        }
        Link {
            to = "/login"
            button {
                css {
                    backgroundColor = Color("#B58863")
                }
                +"Sign up"
            }
        }
        Link {
            to = "/courses"
            button {
                css {
                    backgroundColor = Color("#B58863")
                }
                +"Explore the courses"
            }
        }
    }
}

val UserProfile = FC<Props> {
    h1 {
        +"You're awesome"
    }
}

val App = FC<Props> {
    var coursesList by useState(emptyList<Course>())
    useEffectOnce {
        scope.launch {
            coursesList = getCoursesList()
        }
    }

    RouterProvider {
        router = createHashRouter(
            routes = arrayOf(
                jso {
                    path = "/"
                    Component = Home
                },
                jso {
                    path = "/courses"
                    Component = CoursesList
                },
                jso {
                    path = "/courses/:courseId"
                    loader = { args ->
                        coursesList.find { it.idName == args.params["courseId"] } as Any
                    }
                    Component = CoursePage
                },
                jso {
                    path = "/login"
                    Component = LoginPage
                },
                jso {
                    path = "/me"
                    Component = UserProfile
                },
                jso {
                    path = "/signup"
                    Component = SignupPage
                }
            )
        )
    }
}