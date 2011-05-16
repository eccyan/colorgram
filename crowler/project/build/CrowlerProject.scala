import sbt._

class CrowlerProject(info: ProjectInfo) extends DefaultProject(info) {
    import BasicScalaProject._

    // Repositories
    val downloadJavaNet = "download.java.net" at "http://download.java.net/maven/2/"

    // Dependent jar files
    val orgJsoup = "org.jsoup" % "jsoup" % "1.5.2"
}
