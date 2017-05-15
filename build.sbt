name := "directives2"

organization := "no.shiplog"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "0.9.0",
  "ws.unfiltered" %% "unfiltered" % "0.9.0-beta2",
  "ws.unfiltered" %% "unfiltered-directives" % "0.9.0-beta2" % "optional"
)

crossScalaVersions := Seq("2.12.2", "2.11.8", "2.10.6")

scalaVersion := crossScalaVersions.value.head
