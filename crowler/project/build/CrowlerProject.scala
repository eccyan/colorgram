import sbt._

class CrowlerProject(info: ProjectInfo) extends DefaultProject(info)
    with AssemblyProject {
    import BasicScalaProject._

    // Repositories
    val downloadJavaNet = "download.java.net" at "http://download.java.net/maven/2/"

    // Dependent jar files
    val mysqlConnectorJava = "mysql" % "mysql-connector-java" % "5.1.16"
    val squeryl            = "org.squeryl" % "squeryl_2.8.1" % "0.9.4-RC6"
    val orgJsoup           = "org.jsoup" % "jsoup" % "1.5.2"

    // for create jar
    override def mainClass = Some("colorgram.crowler.Application")
}
