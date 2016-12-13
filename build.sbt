name := "directives2"

organization := "no.shiplog"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.1.11",
  "ws.unfiltered" %% "unfiltered" % "0.9.0-beta2",
  "ws.unfiltered" %% "unfiltered-directives" % "0.9.0-beta2" % "optional"
)

crossScalaVersions := Seq("2.12.1", "2.11.8", "2.10.6")

scalaVersion := crossScalaVersions.value.head
