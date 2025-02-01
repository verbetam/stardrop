ThisBuild / version := "pre-release"

ThisBuild / scalaVersion := "2.13.9"

val circeVersion = "0.14.1"
val catsVersion = "2.8.0"
val scalaTestVersion = "3.2.13"

libraryDependencies += "org.typelevel" %% "cats-core" % catsVersion
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-generic-extras",
  "io.circe" %% "circe-parser",
).map(_ % circeVersion)
libraryDependencies += "org.scalactic" %% "scalactic" % scalaTestVersion
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.13.4"

val slf4jVersion = "1.7.36"
//libraryDependencies += "org.slf4j" % "slf4j-api" % slf4jVersion
//libraryDependencies += "org.slf4j" % "slf4j-simple" % slf4jVersion
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.11"

libraryDependencies += "com.monovore" %% "decline" % "2.3.1"

libraryDependencies += "org.scalatest" %% "scalatest" % scalaTestVersion % Test
libraryDependencies += "org.scalamock" %% "scalamock" % "5.1.0" % Test

scalacOptions += "-Werror"

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case "module-info.class"           => MergeStrategy.discard
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}

lazy val root = (project in file("."))
  .settings(name := "stardrop", idePackagePrefix := Some("quincyjo.stardew"))
