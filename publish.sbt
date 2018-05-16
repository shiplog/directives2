overridePublishBothSettings
overridePublishSignedSettings

val nexusRepos = "https://shiplog.jfrog.io/shiplog/"

publishTo := {
  if (isSnapshot.value) {
    Some("Shiplog Snapshots" at nexusRepos + "libs-snapshot-local/")
  } else {
    Some("Shiplog Releases" at nexusRepos + "libs-release-local/")
  }
}


pomIncludeRepository := { x => false }

packageOptions += {
  val title = name.value
  val ver = version.value
  val vendor = organization.value

  Package.ManifestAttributes(
    "Created-By" -> "Scala Build Tool",
    "Built-By" -> System.getProperty("user.name"),
    "Build-Jdk" -> System.getProperty("java.version"),
    "Specification-Title" -> title,
    "Specification-Version" -> ver,
    "Specification-Vendor" -> vendor,
    "Implementation-Title" -> title,
    "Implementation-Version" -> ver,
    "Implementation-Vendor-Id" -> vendor,
    "Implementation-Vendor" -> vendor
  )
}

credentials ++= Seq(
  Credentials(Path.userHome / ".sbt" / ".sonatype-credentials"),
  Credentials(Path.userHome / ".sbt" / "artifactory.credentials")
)

homepage := Some(url("http://github.com/shiplog/d2"))

startYear := Some(2014)

licenses := Seq(
  "MIT" -> url("https://github.com/shiplog/d2/blob/master/LICENSE.md")
)

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

releaseCrossBuild := true

releasePublishArtifactsAction := PgpKeys.publishSigned.value

scmInfo := Some(ScmInfo(
  new URL("http://github.com/shiplog/d2"),
  "scm:git:git://github.com/shiplog/d2.git",
  Some("scm:git:git@github.com:shiplog/d2.git")
))

developers ++= List(
  Developer(
    "kareblak",
    "Kåre Blakstad",
    "",
    null
  ),
  Developer(
    "teigen",
    "Jon Anders Teigen",
    "",
    null
  ),
  Developer(
    "hamnis",
    "Erlend Hamnaberg",
    "erlend@hamnaberg.net",
    new URL("http://twitter.com/hamnis")
  )
)