
name := "SVApp"

version := "0.1"

scalaVersion := "2.10.0"

resolvers ++= Seq(
  "Vaadin Snapshots" at "https://oss.sonatype.org/content/repositories/vaadin-snapshots/",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/maven-releases/",
  "Java.Net" at "http://download.java.net/maven/2/"
)

scalacOptions ++= Seq("-Xcheckinit")

libraryDependencies ++= Seq(
  "com.vaadin" % "vaadin-server" % "7.0-SNAPSHOT",
  "com.vaadin" % "vaadin-client-compiled" % "7.0-SNAPSHOT",
  "com.vaadin" % "vaadin-themes" % "7.0-SNAPSHOT",
  "javax.servlet" % "javax.servlet-api" % "3.0.1",
  "play" % "anorm_2.9.1" % "2.0.4",
  "javax.mail" % "mail" % "1.4.5",
  "log4j" % "log4j" % "1.2.17",
  "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
  "com.typesafe" % "config" % "1.0.0",
  "com.googlecode.json-simple" % "json-simple" % "1.1.1"
)
