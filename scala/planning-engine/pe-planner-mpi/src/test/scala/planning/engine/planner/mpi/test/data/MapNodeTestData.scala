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
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.text.Description
import planning.engine.map.io.node.{InputNode, OutputNode}
import planning.engine.map.io.variable.{BooleanIoVariable, IntIoVariable}
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.common.data.node.{AbsData, ConData}

trait MapNodeTestData extends AsyncMockFactory:
  self: UnitSpecWithData =>

  trait WithMapNode:
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

    lazy val nim1: MnId.Nim = MnId.Nim(1L)
    lazy val nim2: MnId.Nim = MnId.Nim(2L)
    lazy val conMnId: MnId.Con = MnId.Con(1L)
    lazy val absMnId: MnId.Abs = MnId.Abs(2L)

    def makeNodeStub(id: MnId, name: String = ""): Node =
      makeNodeStub(id, if name.nonEmpty then Some(HnName(name)) else None)

    def makeNodeStub(id: MnId, name: Option[HnName]): Node =
      val st = stub[Node]
      (() => st.mnId).when().returns(id)
      (() => st.name).when().returns(name)
      st
