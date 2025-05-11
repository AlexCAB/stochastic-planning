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

ThisBuild / scalaVersion := "3.7.0"
ThisBuild / scalacOptions ++= Seq(
  "-encoding",
  "utf8",
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Wunused:imports",
  "-Xfatal-warnings",
  "-Wshadow:type-parameter-shadow"
)

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

// Projects settings

lazy val common = project in file("pe-common")

lazy val map = (project in file("pe-map"))
  .dependsOn(
    common % "compile->compile;test->test"
  )

lazy val planner = (project in file("pe-planner"))
  .dependsOn(
    common % "compile->compile;test->test",
    map % "compile->compile;test->test"
  )

lazy val api = (project in file("pe-rest-api"))
  .dependsOn(
    common % "compile->compile;test->test",
    planner % "compile->compile;test->test",
    map % "compile->compile;test->test"
  )

lazy val itAndTools = (project in file("pe-tools-and-it"))
  .dependsOn(
    common % "compile->compile;test->test",
    map % "compile->compile;test->test",
    planner % "compile->compile;test->test",
    api % "compile->compile;test->test"
  )

lazy val root = project
  .in(file("."))
  .aggregate(common, map, api, itAndTools)
