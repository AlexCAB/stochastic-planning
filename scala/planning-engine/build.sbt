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

ThisBuild / scalaVersion := "3.8.3"
ThisBuild / scalacOptions ++= Seq(
  "-encoding",
  "utf8",
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Wunused:imports",
  "-Werror",
  "-Wshadow:type-parameter-shadow"
)

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

// Sub-projects settings

lazy val common = project in file("pe-common")

lazy val map = (project in file("pe-map"))
  .dependsOn(
    common % "compile->compile;test->test"
  )

lazy val planner_gsi = (project in file("pe-planner-gsi"))
  .dependsOn(
    common % "compile->compile;test->test",
    map % "compile->compile;test->test"
  )

lazy val planner_mpi = (project in file("pe-planner-mpi"))
  .dependsOn(
    common % "compile->compile;test->test",
    map % "compile->compile;test->test"
  )

lazy val api = (project in file("pe-rest-api"))
  .dependsOn(
    common % "compile->compile;test->test",
    planner_gsi % "compile->compile;test->test",
    planner_mpi % "compile->compile;test->test",
    map % "compile->compile;test->test"
  )

lazy val itAndTools = (project in file("pe-tools-and-it"))
  .dependsOn(
    common % "compile->compile;test->test",
    map % "compile->compile;test->test",
    planner_gsi % "compile->compile;test->test",
    planner_mpi % "compile->compile;test->test",
    api % "compile->compile;test->test"
  )

// Root project settings

lazy val root = project
  .in(file("."))
  .aggregate(common, map, planner_gsi, planner_mpi, api, itAndTools)
