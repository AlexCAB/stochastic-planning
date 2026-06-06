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

val akkaVersion = "2.0.0-M3"

// Subprojects settings

name := "pe-planner-mpi"
description := "Message propagation planner implementation"
version := "0.0.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-actor-typed" % akkaVersion,
  "org.apache.pekko" %% "pekko-actor-testkit-typed" % akkaVersion % Test,
)
