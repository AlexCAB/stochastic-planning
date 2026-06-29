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
| created: 09.06.2026 |||||||||||*/

package planning.engine.planner.mpi.test.data

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.text.Description
import planning.engine.map.io.node.{InputNode, OutputNode}
import planning.engine.map.io.variable.{BooleanIoVariable, IntIoVariable}
import planning.engine.planner.mpi.actors.node.NodeActor.{AbsDef, ConDef}
import planning.engine.planner.mpi.data.node.{AbsData, ConData, StaticActors}

trait MapNodeTestData:
  private implicit lazy val ioRuntime: IORuntime = IORuntime.global

  lazy val testBoolInNode = InputNode[IO](IoName("boolInputNode"), BooleanIoVariable[IO](Set(true, false)))
  lazy val testIntInNode = InputNode[IO](IoName("intInputNode"), IntIoVariable[IO](0, 10000))
  lazy val boolOutNode = OutputNode[IO](IoName("boolOutputNode"), BooleanIoVariable[IO](Set(true, false)))

  lazy val conNodeData: ConData = ConData(
    name = Some(HnName("Test Concrete Node")),
    description = Some(Description("A test node for unit testing`.")),
    ioName = testBoolInNode.name,
    valueIndex = IoIndex(0),
  )

  lazy val absNodeData: AbsData = AbsData(
    name = Some(HnName("Test Abstract Node")),
    description = Some(Description("A test abstract node for unit testing.")),
  )

  def conDefData(actors: StaticActors): ConDef = ConDef(
    id = MnId.Con(1L),
    data = conNodeData,
    actors = actors,
  )

  def absDefData(actors: StaticActors): AbsDef = AbsDef(
    id = MnId.Abs(2L),
    data = absNodeData,
    actors = actors,
  )
