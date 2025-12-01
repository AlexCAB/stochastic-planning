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
import planning.engine.common.values.node.{HnId, IoIndex, SnId}
import planning.engine.common.values.text.Name
import planning.engine.map.io.node.{InputNode, OutputNode}
import planning.engine.map.io.variable.IntIoVariable
import planning.engine.planner.plan.dag.{AbstractStateNode, ConcreteStateNode, StateNode}

trait DagTestData:
  private implicit lazy val ioRuntime: IORuntime = IORuntime.global

  lazy val intInNodes =
    (1 to 10).toList.map(i => InputNode[IO](Name(s"intInputNode$i"), IntIoVariable[IO](-10000, 10000)))

  lazy val intOutNode = OutputNode[IO](Name("intOutputNode"), IntIoVariable[IO](-10000, 10000))

  lazy val conId: SnId = SnId(1L)
  lazy val absId: SnId = SnId(2L)
  lazy val conHnId: HnId = HnId(10001)
  lazy val absHnId: HnId = HnId(10002)
  lazy val conName: Option[Name] = Some(Name("ConTestNode"))
  lazy val absName: Option[Name] = Some(Name("AbsTestNode"))
  lazy val valueIndex: IoIndex = IoIndex(432L)

  lazy val absStateNode1 = AbstractStateNode[IO]
    .apply(absId, absHnId, absName, Set(), Set(), StateNode.Parameters.init)
    .unsafeRunSync()

  lazy val conStateNode1 = ConcreteStateNode[IO]
    .apply(conId, conHnId, conName, intInNodes.head, valueIndex, Set(), Set(), StateNode.Parameters.init)
    .unsafeRunSync()
