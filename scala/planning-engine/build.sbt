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

// Common settings

ThisBuild / scalaVersion := "3.6.3"
ThisBuild / scalacOptions ++= Seq(
  "-encoding", "utf8",
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Wunused:imports"
)

// Projects settings

lazy val common = (project in file("pe-common"))

lazy val core = (project in file("pe-core"))
  .dependsOn(
    common % "compile->compile;test->test"
  )

lazy val api = (project in file("pe-rest-api"))
  .dependsOn(
    common % "compile->compile;test->test",
    core % "compile->compile;test->test"
  )

lazy val itAndTools = (project in file("pe-tools-and-it"))
  .dependsOn(
    common % "compile->compile;test->test",
    core % "compile->compile;test->test",
    api % "compile->compile;test->test"
  )

lazy val root = project
  .in(file("."))
  .aggregate(common, core, api, itAndTools)
