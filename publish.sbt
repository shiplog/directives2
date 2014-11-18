aetherPublishSettings

val nexusRepos = "http://dev.shiplog.no:8080/nexus/content/repositories"

publishTo <<= version apply {
  (v: String) => if (v.trim().endsWith("SNAPSHOT")) {
    Some("Shiplog Snapshots" at nexusRepos + "/snapshots/")
  } else {
    Some("Shiplog Releases" at nexusRepos + "/releases/")
  }
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

credentials += Credentials(Path.userHome / ".sbt" / ".credentials")

homepage := Some(new URL("http://github.com/shiplog/d2"))

startYear := Some(2014)

licenses := Seq(("Apache 2", new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")))

pomExtra <<= (pomExtra, name, description) {(pom, name, desc) => pom ++ xml.Group(
  <scm>
    <url>http://github.com/shiplog/d2</url>
    <connection>scm:git:git://github.com/shiplog/d2.git</connection>
    <developerConnection>scm:git:git@github.com:shiplog/d2.git</developerConnection>
  </scm>
  <developers>
    <developer>
      <id>teigen</id>
      <name>Jon Anders Teigen</name>
    </developer>
    <developer>
      <id>kareblak</id>
      <name>Kåre Blakstad</name>
    </developer>
  </developers>
)}
