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

import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.io.{IoIndex, IoName, IoValue}
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.text.Description
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.model.data.node.NodeData
import planning.engine.planner.mpi.model.io.{Type, Variable}

trait MapNodeTestData extends AsyncMockFactory:
  self: UnitSpecWithData =>

  trait WithMapNode:
    lazy val inVarName: IoName = IoName("boolInputNode")
    lazy val outVarName: IoName = IoName("boolOutputNode")

    lazy val intType: Type.N = Type.N(-100, 100)
    lazy val boolType: Type.Bool = Type.Bool(Set(true, false))

    lazy val inVar: Variable.Input = Variable.Input(inVarName, intType)
    lazy val outVar: Variable.Output = Variable.Output(outVarName, boolType)

    def makeConNodeStub(mnId: MnId.Con, name: IoName, index: IoIndex): Node.Con =
      val st = stub[Node.Con]
      (() => st.mnId).when().returns(mnId)
      (() => st.name).when().returns(None)
      (() => st.ioValue).when().returns(IoValue(name, index))
      st

    lazy val inConNode: Node.Con = makeConNodeStub(MnId.Con(1L), inVarName, IoIndex(0))
    lazy val outConNode: Node.Con = makeConNodeStub(MnId.Con(2L), outVarName, IoIndex(0))
    lazy val otherInConNode: Node.Con = makeConNodeStub(MnId.Con(3L), inVarName, IoIndex(1))

    lazy val conNodeData: NodeData.Con = NodeData.Con(
      name = Some(HnName("Test Concrete Node")),
      description = Some(Description("A test node for unit testing`.")),
      ioName = inVar.name,
      valueIndex = IoIndex(0),
    )

    lazy val absNodeData: NodeData.Abs = NodeData.Abs(
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
