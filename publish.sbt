import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport._

aetherPublishSettings

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomIncludeRepository := { x => false }

packageOptions <+= (name, version, organization) map {
  (title, version, vendor) =>
    Package.ManifestAttributes(
      "Created-By" -> "Scala Build Tool",
      "Built-By" -> System.getProperty("user.name"),
      "Build-Jdk" -> System.getProperty("java.version"),
      "Specification-Title" -> title,
      "Specification-Version" -> version,
      "Specification-Vendor" -> vendor,
      "Implementation-Title" -> title,
      "Implementation-Version" -> version,
      "Implementation-Vendor-Id" -> vendor,
      "Implementation-Vendor" -> vendor
    )
}

credentials += Credentials(Path.userHome / ".sbt" / ".sonatype-credentials")

homepage := Some(url("http://github.com/shiplog/d2"))

startYear := Some(2014)

licenses := Seq(
  "MIT" -> url("https://github.com/shiplog/d2/blob/master/LICENSE.md")
)

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

useGpg := true

releaseCrossBuild := true

pomExtra <<= (pomExtra, name, description) {(pom, name, desc) => pom ++ xml.Group(
  <scm>
    <url>http://github.com/shiplog/d2</url>
    <connection>scm:git:git://github.com/shiplog/d2.git</connection>
    <developerConnection>scm:git:git@github.com:shiplog/d2.git</developerConnection>
  </scm>
  <developers>
    <developer>
      <id>kareblak</id>
      <name>Kåre Blakstad</name>
    </developer>
    <developer>
      <id>teigen</id>
      <name>Jon Anders Teigen</name>
    </developer>
    <developer>
      <id>hamnis</id>
      <name>Erlend Hamnaberg</name>
    </developer>
  </developers>
)}
