name := "directives2"

organization := "no.shiplog"

version := "0.9.2"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.1.2",
  "net.databinder" %% "unfiltered" % "0.8.4",
  "net.databinder" %% "unfiltered-directives" % "0.8.4" % "optional"
)

crossScalaVersions := Seq("2.10.4", "2.11.6")

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")
