name := "directives2"

organization := "no.shiplog"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.0.0-RC1",
  "ws.unfiltered" %% "unfiltered" % "0.9.1",
  "ws.unfiltered" %% "unfiltered-directives" % "0.9.1" % "optional"
)

crossScalaVersions := Seq("2.12.4", "2.11.8", "2.10.6")

scalaVersion := crossScalaVersions.value.head
