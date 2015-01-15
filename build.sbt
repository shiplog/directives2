name := "directives2"

organization := "no.shiplog"

version := "0.9"

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.1.0",
  "net.databinder" %% "unfiltered" % "0.8.3"
)

crossScalaVersions := Seq("2.10.4", "2.11.4")

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")