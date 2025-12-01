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
| created: 2025-08-26 |||||||||||*/

package planning.engine.planner.context

import cats.effect.IO
import planning.engine.common.UnitSpecWithData
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.planner.dag.DagTestData
import cats.effect.cps.*
import planning.engine.common.values.node.{IoIndex, SnId}
import planning.engine.common.values.text.Name
import planning.engine.map.io.node.InputNode
import planning.engine.planner.plan.dag.{ConcreteStateNode, PlanningDagLike, StateNode}
import planning.engine.planner.plan.dag.StateNode.{Kind, Parameters}

class SimpleContextSpec extends UnitSpecWithData with DagTestData with AsyncMockFactory:

  private class CaseData extends Case:
    val planningDagMock = mock[PlanningDagLike[IO]]

    lazy val context = SimpleContext[IO]
      .apply(maxPathLength = 10, planningDagMock)
      .unsafeRunSync()

    lazy val presentParams: Parameters = Parameters.init.copy(kind = Kind.Present)
    lazy val planParams: Parameters = Parameters.init.copy(kind = Kind.Plan)

    def makeNode(
        id: Long,
        ioNode: InputNode[IO],
        params: Parameters,
        parents: Set[StateNode[IO]] = Set()
    ): IO[ConcreteStateNode[IO]] =
      ConcreteStateNode[IO](SnId(id), conHnId, None, ioNode, IoIndex(id), Set(), parents, params)

  "SimpleContext.moveNextFoundIntoContext(...)" should:
    "move next found state nodes into context" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val present11 = data.makeNode(11L, intInNodes.head, data.presentParams).await
        val present12 = data.makeNode(12L, intInNodes(1), data.presentParams).await
        val present13 = data.makeNode(13L, intInNodes(2), data.presentParams).await
        val present14 = data.makeNode(14L, intInNodes(3), data.presentParams).await
        val present15 = data.makeNode(15L, intInNodes(4), data.presentParams).await
        val allPresents = Set[StateNode[IO]](present11, present12, present13, present14, present15)

        val plan21 = data.makeNode(21L, intInNodes(5), data.planParams, Set(present11, present12)).await
        val plan22 = data.makeNode(22L, intInNodes(6), data.planParams, Set(present13)).await
        val plan23 = data.makeNode(23L, intInNodes(7), data.planParams, Set(present13)).await
        val plan24 = data.makeNode(24L, intInNodes(8), data.planParams, Set(present14)).await
        val plan25 = data.makeNode(25L, intInNodes(9), data.planParams, Set(present15)).await

        logInfo(tn, s"For: $present11, children: ${present11.getStructure.await.thenChildren}").await

        data.planningDagMock.modifyContextBoundary.expects(*)
          .onCall: modifyFun =>
            for
              (newCon, res) <- modifyFun(allPresents)
              _ <- logInfo(tn, s"New context boundary: $newCon, result: $res")
              _ = newCon mustEqual Set(plan21, plan22, plan23, plan24, present15)
            yield res
          .once()

        val otherValue = Name("otherIONOde") -> IoIndex(999L)
        val values = List(plan21, plan22, plan23, plan24).map(n => n.ioNode.name -> n.valueIndex).toMap.+(otherValue)

        val notFoundValues: Map[Name, IoIndex] = data.context.moveNextFoundIntoContext(values).await

        notFoundValues mustEqual Map(otherValue)
