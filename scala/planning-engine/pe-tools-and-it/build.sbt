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

val scalaFxVersion = "27.0.0-R39"
val smartGraphVersion = "2.3.0"
val scoptVersion = "4.1.0"

// Subprojects settings

name := "pe-tools-and-it"
description := "Tools, utils and integration tests"
version := "0.0.0-SNAPSHOT"


libraryDependencies ++= Seq(
  "org.scalafx" %% "scalafx" % scalaFxVersion,
  "com.brunomnsilva" % "smartgraph" % smartGraphVersion,
  "com.github.scopt" %% "scopt" % scoptVersion,
)

