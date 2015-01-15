name := "directives2"

organization := "no.shiplog"

version := "0.9"

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.1.0",
  "net.databinder" %% "unfiltered" % "0.8.3"
)

licenses := Seq(
  "MIT" -> url("https://github.com/shiplog/d2/blob/master/LICENSE.md")
)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

useGpg := true