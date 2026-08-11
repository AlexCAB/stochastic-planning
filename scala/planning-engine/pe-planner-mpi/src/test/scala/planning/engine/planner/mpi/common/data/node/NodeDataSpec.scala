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
| created: 30.06.2026 |||||||||||*/

package planning.engine.planner.mpi.common.data.node

import cats.effect.IO
import cats.syntax.all.*
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.test.data.MapNodeTestData

class NodeDataSpec extends UnitSpecWithIOAndTestKit:
  private class CaseData extends Case with MapNodeTestData

  "NodeData.Kit.getUniqueNames(...)" should:
    "return a set of names for all named nodes" in newCase[CaseData]: (_, data) =>
      NodeData(data.conNodeData, data.absNodeData).getUniqueNames[IO]
        .asserting(_ mustBe Set.from(List(data.conNodeData.name, data.absNodeData.name).flatten))

    "raise an error when nodes have duplicate names" in newCase[CaseData]: (_, data) =>
      NodeData(data.conNodeData, data.conNodeData.copy(description = None)).getUniqueNames[IO]
        .assertThrows[AssertionError]

  "NodeData.Kit.filterNotByNames(...)" should:
    "remove nodes whose name is in the given set" in newCase[CaseData]: (_, data) =>
      NodeData(data.conNodeData, data.absNodeData)
        .filterNotByNames(Set(data.conNodeData.name.get)).pure[IO]
        .asserting(_ mustBe NodeData.Kit(List(data.absNodeData)))

    "keep all nodes when the name set is empty" in newCase[CaseData]: (_, data) =>
      NodeData(data.conNodeData, data.absNodeData)
        .filterNotByNames(Set.empty).pure[IO]
        .asserting(_ mustBe NodeData.Kit(List(data.conNodeData, data.absNodeData)))

    "keep nodes with no name even when their absence matches a filtered name" in newCase[CaseData]: (_, data) =>
      val unnamedNode = data.absNodeData.copy(name = None)
      NodeData(data.conNodeData, unnamedNode)
        .filterNotByNames(Set(data.conNodeData.name.get)).pure[IO]
        .asserting(_ mustBe NodeData.Kit(List(unnamedNode)))
