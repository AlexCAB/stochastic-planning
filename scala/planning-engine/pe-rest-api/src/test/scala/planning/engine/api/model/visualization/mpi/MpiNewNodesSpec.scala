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
| created: 2026-10-08 |||||||||||*/

package planning.engine.api.model.visualization.mpi

import cats.effect.IO
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.common.values.node.{HnId, HnName, MnId}
import planning.engine.planner.mpi.model.data.node.NodeData

class MpiNewNodesSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    lazy val ioNameA: IoName = IoName("ioA")
    lazy val ioNameB: IoName = IoName("ioB")

    def conData(ioName: IoName): NodeData = NodeData.Con(Some(HnName(s"con-$ioName")), None, ioName, IoIndex(0L))
    lazy val absData: NodeData = NodeData.Abs(Some(HnName("abs")), None)

  "MpiNewNodes.apply(...)" should:
    "split nodes into concrete and abstract IDs and group concrete IDs by IO name" in newCase[CaseData]: (tn, data) =>
      import data.*
      val ids: Map[MnId, NodeData] = Map(
        MnId.Con(1L) -> conData(ioNameA),
        MnId.Con(2L) -> conData(ioNameA),
        MnId.Con(3L) -> conData(ioNameB),
        MnId.Abs(4L) -> absData,
      )

      MpiNewNodes[IO](ids).logValue(tn).asserting(_ mustEqual MpiNewNodes(
        conIds = Set(HnId(1L), HnId(2L), HnId(3L)),
        absIds = Set(HnId(4L)),
        ioEdges = Set(ioNameA -> Set(HnId(1L), HnId(2L)), ioNameB -> Set(HnId(3L))),
      ))

    "fail if node ID type doesn't match node data type" in newCase[CaseData]: (tn, data) =>
      import data.*
      MpiNewNodes[IO](Map(MnId.Con(1L) -> absData)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Unexpected MnId type"))
