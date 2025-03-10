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

val scala3Version = "3.6.3"
val neoTypesCatsVersion = "1.2.1"
val pureConfigCoreVersion = "0.17.8"
val scalatestCoreVersion = "3.2.19"
val catsCoreVersion = "2.13.0"
val catsEffectVersion = "3.5.7"

// Project settings

lazy val root = project
  .in(file("."))
  .settings(
    name := "planning-engine",
    version := "0.0.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "com.github.pureconfig" %% "pureconfig-core" % pureConfigCoreVersion,
      "com.github.pureconfig" %% "pureconfig-cats-effect" % pureConfigCoreVersion,
      "com.github.pureconfig" %% "pureconfig-generic-scala3" % pureConfigCoreVersion,

      "org.typelevel" %% "cats-core" % catsCoreVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion,

      "io.github.neotypes" %% "neotypes-cats-effect" % neoTypesCatsVersion,
      "io.github.neotypes" %% "neotypes-cats-data" % neoTypesCatsVersion,

      "org.scalactic" %% "scalactic" % scalatestCoreVersion % Test,
      "org.scalatest" %% "scalatest" % scalatestCoreVersion % Test
    )
  )
