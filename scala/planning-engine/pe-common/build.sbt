/*|||||||||||||||||||||||||||||||||
|| 0 * * * * * * * * * ▲ * * * * ||
|| * ||||||||||| * ||||||||||| * ||
|| * ||  * * * * * ||       || 0 ||
|| * ||||||||||| * ||||||||||| * ||
|| * * ▲ * * 0|| * ||   (< * * * ||
|| * ||||||||||| * ||  ||||||||||||
|| * * * * * * * * *   ||||||||||||
| author: CAB |||||||||||||||||||||
| website: github.com/alexcab |||||
| created: 2025-03-10 |||||||||||*/

//  Versions

val slf4jVersion = "2.0.17"
val logbackVersion = "1.5.18"
val log4catsSlf4jVersion = "2.7.0"
val pureConfigCoreVersion = "0.17.8"
val catsCoreVersion = "2.13.0"
val neo4jJavaDriverVersion = "5.28.3"
val catsEffectVersion = "3.5.7"
val specs2CoreVersion = "4.20.9"
val catsEffectTestingScalatestVersion = "1.6.0"
val catsEffectCpsVersion = "0.3.0"

// Subprojects settings

name := "pe-common"
version := "0.0.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "ch.qos.logback" % "logback-core" % logbackVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "org.typelevel" %% "log4cats-slf4j" % log4catsSlf4jVersion,

  "com.github.pureconfig" %% "pureconfig-core" % pureConfigCoreVersion,
  "com.github.pureconfig" %% "pureconfig-cats-effect" % pureConfigCoreVersion,
  "com.github.pureconfig" %% "pureconfig-generic-scala3" % pureConfigCoreVersion,

  "org.typelevel" %% "cats-core" % catsCoreVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion,

  "org.neo4j.driver" % "neo4j-java-driver" % neo4jJavaDriverVersion,

  "org.specs2" %% "specs2-core" % specs2CoreVersion % Test,

  "org.typelevel" %% "cats-effect-testing-specs2" % catsEffectTestingScalatestVersion % Test,
  "org.typelevel" %% "cats-effect-cps" % catsEffectCpsVersion % Test,
)
