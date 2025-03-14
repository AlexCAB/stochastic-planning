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

val neoTypesCatsVersion = "1.2.1"

// Sub projects settings

name := "pe-core"
version := "0.0.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "io.github.neotypes" %% "neotypes-core" % neoTypesCatsVersion,
  "io.github.neotypes" %% "neotypes-cats-effect" % neoTypesCatsVersion,
  "io.github.neotypes" %% "neotypes-cats-data" % neoTypesCatsVersion,
  "io.github.neotypes" %% "neotypes-generic" % neoTypesCatsVersion,
)
