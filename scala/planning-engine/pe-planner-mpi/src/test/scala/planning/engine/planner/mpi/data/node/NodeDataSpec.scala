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

package planning.engine.planner.mpi.data.node

import cats.effect.IO
import cats.syntax.all.*
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.node.NodeActor
import planning.engine.planner.mpi.test.actors.StaticTestActors
import planning.engine.planner.mpi.test.data.MapNodeTestData

class NodeDataSpec extends UnitSpecWithIOAndTestKit with StaticTestActors:
  private class CaseData extends Case with MapNodeTestData with WithStaticActors

  "NodeData.toDefinition(...)" should:
    "for ConData, return ConDef with given raw id and actors" in newCase[CaseData]: (_, data) =>
      data.conNodeData.toDefinition[IO](1L, data.staticActors)
        .asserting(_ mustBe NodeActor.ConDef(MnId.Con(1L), data.conNodeData, data.staticActors))

    "for AbsData, return AbsDef with given raw id and actors" in newCase[CaseData]: (_, data) =>
      data.absNodeData.toDefinition[IO](2L, data.staticActors)
        .asserting(_ mustBe NodeActor.AbsDef(MnId.Abs(2L), data.absNodeData, data.staticActors))

  "NodeData.Kit.toDefinitions(...)" should:
    "return definitions with incremented raw ids for a kit with multiple nodes" in newCase[CaseData]: (_, data) =>
      NodeData(data.conNodeData, data.absNodeData).toDefinitions[IO](1L, data.staticActors)
        .asserting(_ mustBe List(
          NodeActor.ConDef(MnId.Con(1L), data.conNodeData, data.staticActors),
          NodeActor.AbsDef(MnId.Abs(2L), data.absNodeData, data.staticActors),
        ))

    "raise an error for a kit with duplicate nodes" in newCase[CaseData]: (_, data) =>
      NodeData(data.conNodeData, data.conNodeData).toDefinitions[IO](1L, data.staticActors)
        .assertThrows[AssertionError]

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
