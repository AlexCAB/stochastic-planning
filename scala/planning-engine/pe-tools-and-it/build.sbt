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

val ScalaFxVersion = "27.0.0-R39"

// Subprojects settings

name := "pe-tools-and-it"
description := "Tools, utils and integration tests"
version := "0.0.0-SNAPSHOT"


libraryDependencies ++= Seq(
  "org.scalafx" %% "scalafx" % ScalaFxVersion,
)

