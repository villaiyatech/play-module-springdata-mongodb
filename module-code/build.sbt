name := """play-module-springdata-mongodb"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  // mongodb
  "org.springframework.data" % "spring-data-mongodb" % "1.8.4.RELEASE",

  // dependency injection
  "javax.inject" % "javax.inject" % "1"
)

