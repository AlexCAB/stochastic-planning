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

val pureConfigCoreVersion = "0.17.8"
val catsCoreVersion = "2.13.0"
val neo4jJavaDriverVersion = "5.28.2"
val catsEffectVersion = "3.5.7"
val scalatestCoreVersion = "3.2.19"
val catsEffectTestingScalatestVersion = "1.6.0"

// Sub projects settings

name := "pe-common"
version := "0.0.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig-core" % pureConfigCoreVersion,
  "com.github.pureconfig" %% "pureconfig-cats-effect" % pureConfigCoreVersion,
  "com.github.pureconfig" %% "pureconfig-generic-scala3" % pureConfigCoreVersion,

  "org.typelevel" %% "cats-core" % catsCoreVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion,

  "org.neo4j.driver" % "neo4j-java-driver" % neo4jJavaDriverVersion,

  "org.scalactic" %% "scalactic" % scalatestCoreVersion % Test,
  "org.scalatest" %% "scalatest" % scalatestCoreVersion % Test,

  "org.typelevel" %% "cats-effect-testing-scalatest" % catsEffectTestingScalatestVersion % Test,
)
