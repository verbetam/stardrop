ThisBuild / tlBaseVersion := "0.1"
ThisBuild / version := "0.1.0"
ThisBuild / scalaVersion := "2.13.16"
ThisBuild / organization := "com.quincyjo"
ThisBuild / organizationName := "Quincy Jo"
ThisBuild / organizationHomepage := Some(url("https://quincyjo.com"))
ThisBuild / homepage := Some(url("https://github.com/quincyjo/stardrop"))
ThisBuild / startYear := Some(2023)
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/quincyjo/stardrop"),
    "git@github.com:quincyjo/stardrop.git"
  )
)
ThisBuild / developers := List(
  Developer(
    "quincyjo",
    "Quincy Jo",
    "me@quincyjo.com",
    url("https://github.com/quincyjo")
  )
)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / tlJdkRelease := Some(11)

Global / excludeLintKeys += tlBaseVersion

val circeVersion = "0.14.1"
val catsVersion = "2.13.0"
val catsEffectVersion = "3.5.7"
val scalaTestVersion = "3.2.19"
val declineVersion = "2.5.0"
val scalamockVersion = "6.1.1"
val jacksonVersion = "2.18.2"
val slf4jVersion = "1.7.36"
val logbackVersion = "1.5.16"

lazy val catsCore = "org.typelevel" %% "cats-core" % catsVersion
lazy val catsEffect = "org.typelevel" %% "cats-effect" % catsEffectVersion
lazy val circe = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-generic-extras",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)
lazy val scalactic = "org.scalactic" %% "scalactic" % scalaTestVersion
lazy val jackson =
  "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion
// lazy val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jVersion
// lazy val slf4jSimple = "org.slf4j" % "slf4j-simple" % slf4jVersion
lazy val logback = "ch.qos.logback" % "logback-classic" % logbackVersion
lazy val decline = "com.monovore" %% "decline" % declineVersion
lazy val scalatest = "org.scalatest" %% "scalatest" % scalaTestVersion % Test
lazy val scalamock = "org.scalamock" %% "scalamock" % scalamockVersion % Test

val commonSettings = Seq(
  libraryDependencies ++= Seq(
    catsCore,
    catsEffect,
    scalactic,
    jackson,
    logback,
    scalatest,
    scalamock
  ) ++ circe,
  scalacOptions ++= Seq(
    "-feature",
    "-language:implicitConversions",
    "-Werror",
    "-Xfatal-warnings"
  )
)

val assemblySettings = Seq(
  assembly / mainClass := Some("com.quincyjo.stardrop.cli.App"),
  assembly / assemblyJarName := "stardrop.jar",
  assembly / assemblyMergeStrategy := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case "module-info.class"           => MergeStrategy.discard
    case x =>
      val oldStrategy = (assembly / assemblyMergeStrategy).value
      oldStrategy(x)
  }
)

lazy val root = tlCrossRootProject
  .aggregate(core, alternativeTextures, customFurniture, converters, cli)

lazy val core = project
  .disablePlugins(sbtassembly.AssemblyPlugin)
  .in(file("modules/core"))
  .settings(
    name := "Stardrop Core",
    moduleName := "stardrop-core",
    commonSettings
  )

lazy val alternativeTextures = project
  .disablePlugins(sbtassembly.AssemblyPlugin)
  .in(file("modules/alternative-textures"))
  .dependsOn(core)
  .settings(
    libraryDependencies += decline,
    name := "Stardrop Alternative Textures",
    moduleName := "stardrop-alternative-textures",
    commonSettings
  )

lazy val customFurniture = project
  .disablePlugins(sbtassembly.AssemblyPlugin)
  .in(file("modules/custom-furniture"))
  .dependsOn(core)
  .settings(
    libraryDependencies += decline,
    name := "Stardrop Custom Furniture",
    moduleName := "stardrop-custom-furniture",
    commonSettings
  )

lazy val converters = project
  .disablePlugins(sbtassembly.AssemblyPlugin)
  .in(file("modules/converters"))
  .dependsOn(core, alternativeTextures, customFurniture)
  .settings(
    libraryDependencies += decline,
    name := "Stardrop Converters",
    moduleName := "stardrop-converters",
    commonSettings
  )

lazy val cli = project
  .in(file("modules/cli"))
  .dependsOn(core, converters)
  .settings(
    publish / skip := true,
    libraryDependencies += decline,
    name := "Stardrop Cli",
    moduleName := "stardrop-cli",
    commonSettings,
    assemblySettings
  )
