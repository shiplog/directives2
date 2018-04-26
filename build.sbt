name := "directives2"

organization := "no.shiplog"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.1.0",
  "ws.unfiltered" %% "unfiltered" % "0.9.1",
  "ws.unfiltered" %% "unfiltered-directives" % "0.9.1" % "optional"
)

crossScalaVersions := Seq("2.12.4", "2.11.8")

scalaVersion := crossScalaVersions.value.head
