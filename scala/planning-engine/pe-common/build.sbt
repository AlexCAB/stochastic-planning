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
val pureConfigCoreVersion = "0.17.9"
val catsCoreVersion = "2.13.0"
val neo4jJavaDriverVersion = "5.28.5"
val neoTypesCatsVersion = "1.2.2"
val catsEffectVersion = "3.6.3"
val fs2Version = "3.12.0"
val scalatestVersion = "3.2.19"
val catsEffectTestingScalatestVersion = "1.7.0"
val catsEffectCpsVersion = "0.5.0"
val scalamockVersion = "7.5.2"

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

  "co.fs2" %% "fs2-core" % fs2Version,

  "org.neo4j.driver" % "neo4j-java-driver" % neo4jJavaDriverVersion,

  "io.github.neotypes" %% "neotypes-core" % neoTypesCatsVersion,
  "io.github.neotypes" %% "neotypes-cats-effect" % neoTypesCatsVersion,
  "io.github.neotypes" %% "neotypes-cats-data" % neoTypesCatsVersion,
  "io.github.neotypes" %% "neotypes-generic" % neoTypesCatsVersion,

  "org.scalatest" %% "scalatest-wordspec" % scalatestVersion % Test,
  "org.scalatest" %% "scalatest-mustmatchers" % scalatestVersion % Test,
  "org.typelevel" %% "cats-effect-testing-scalatest" % catsEffectTestingScalatestVersion % Test,
  "org.typelevel" %% "cats-effect-cps" % catsEffectCpsVersion % Test,
  "org.scalamock" %% "scalamock-cats-effect" % scalamockVersion % Test,
)
