import emotion.react.css
import kotlinx.browser.window
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.p
import react.router.dom.Link
import react.router.useLoaderData
import react.useEffectOnce
import react.useState
import web.cssom.Color
import web.cssom.Position
import web.cssom.px

external interface CourseProps: Props {
    var idName: String
    var name: String
    var description: String
}

val CoursePage = FC<Props> {
    val course = useLoaderData().unsafeCast<Course>()
    var courseText by useState("")
    useEffectOnce {
        scope.launch {
            courseText = getCourseText(User(TokenManager.user), course.idName)
        }
    }

    if (!TokenManager.isAuth())
        window.location.href = "http://127.0.0.1:9090/#/login"
    h3 {
        +course.name
    }
    p {
        +course.description
    }
    p {
        if (courseText == "")
            +"Are you interested? You can buy it."
        else
            +courseText
    }
}

val CoursePanel = FC<CourseProps> { courseData ->
    val course = Course(courseData.idName, courseData.name, courseData.description)
    div {
        css {
            backgroundColor = Color("#F0D9B5")
        }
        key = course.idName
        h2 {
            css {
                paddingTop = 10.px
                paddingLeft = 10.px
            }
            +course.name
            Link {
                to = "/courses/${courseData.idName}"
                button {
                    css {
                        backgroundColor = Color("#B58863")
                        position = Position.absolute
                        right = 20.px
                    }
                    +"Learn more"
                }
            }
        }
        p {
            css {
                paddingLeft = 10.px
                paddingBottom = 10.px
            }
            +course.description
        }
    }
}