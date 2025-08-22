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
| created: 2025-08-22 |||||||||||*/

package planning.engine.planner.dag

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import planning.engine.common.values.text.Name
import planning.engine.map.io.node.{InputNode, OutputNode}
import planning.engine.map.io.variable.IntIoVariable

trait DagTestData:
  private implicit lazy val ioRuntime: IORuntime = IORuntime.global

  lazy val intInNode = InputNode[IO](Name("intInputNode"), IntIoVariable[IO](-10000, 10000))
  lazy val intOutNode = OutputNode[IO](Name("intOutputNode"), IntIoVariable[IO](-10000, 10000))

