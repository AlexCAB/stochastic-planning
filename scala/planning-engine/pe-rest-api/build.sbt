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

val http4sVersion = "1.0.0-M44"
val circeVersion = "0.14.12"

// Subprojects settings

name := "pe-rest-api"
version := "0.0.0-SNAPSHOT"

Test / fork := true
Test / envVars := Map("APP_VERSION" -> "test_app_version")

libraryDependencies ++= Seq(

  "org.http4s" %% "http4s-ember-client" % http4sVersion,
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl"          % http4sVersion,

  "io.circe"   %% "circe-generic" % circeVersion,
  "io.circe"   %% "circe-literal" % circeVersion,
  "io.circe"   %% "circe-parser"  % circeVersion,
  "org.http4s" %% "http4s-circe"  % http4sVersion,
)
