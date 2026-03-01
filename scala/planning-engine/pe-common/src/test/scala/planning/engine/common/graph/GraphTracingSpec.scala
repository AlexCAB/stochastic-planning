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
| created: 2026-03-02 |||||||||||*/

package planning.engine.common.graph

import cats.effect.IO
import cats.effect.cps.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.graph.edges.EdgeKey

import planning.engine.common.graph.paths.Path
import planning.engine.common.values.node.MnId

class GraphTracingSpec extends UnitSpecWithData:
  import EdgeKey.{Link, Then}
  import MnId.{Abs, Con}

  private class CaseData extends Case with GraphStructureTestData

  "GraphStructure.findNextLinks(...)" should:
    "return next edges from given mnIds" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        simpleGraph.findNextLinks(Set(c1), simpleGraph.srcLinkMap) mustBe Set(c1 -> Link.End(a4), c1 -> Link.End(a6))
        simpleGraph.findNextLinks(Set(a4), simpleGraph.srcLinkMap) mustBe Set(a4 -> Link.End(a5))
        simpleGraph.findNextLinks(Set(c3), simpleGraph.srcLinkMap) mustBe Set()

  "GraphStructure.traceAbsForestLayers(...)" should:
    "trace abstract nodes from connected mnIds" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        simpleGraph.traceAbsForestLayers(Set(c1)).await mustBe List(
          Set(Link(c1, a4), Link(c1, a6)),
          Set(Link(a4, a5))
        )

        complexGraph.traceAbsForestLayers(Set(c1, c2, c3)).await mustBe List(
          Set(Link(c1, a4), Link(c2, a4), Link(c3, a5)),
          Set(Link(a4, a6), Link(a5, a6))
        )

    "fail if cycle found" in newCase[CaseData]: (tn, data) =>
      import data.*

      invalidLinkGraph.traceAbsForestLayers(Set(c1)).logValue(tn)
        .assertThrowsError(_.getMessage must include("Cycle detected"))

    "fail if invalid edge found" in newCase[CaseData]: (tn, data) =>
      import data.*
      val invalidGraph = GraphStructure[IO](Set(Link(c1, a4), Link(a4, c2)))

      invalidGraph.traceAbsForestLayers(Set(c1)).logValue(tn)
        .assertThrowsError(_.getMessage must include("Found LINK pointed on concrete node"))

  "GraphStructure.traceThenPaths" should:
    "trace direct path (with no loops)" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        simpleGraph.traceThenPaths(Set(c1)).await mustBe (
          Set(Path.Direct(pathWalk(Then(c1, c2))), Path.Direct(pathWalk(Then(c1, c3)))),
          Set(c1, c2, c3)
        )

        complexGraph.traceThenPaths(Set(c1)).await mustBe (
          Set(Path.Direct(pathWalk(Then(c1, c2), Then(c2, c3)))),
          Set(c1, c2, c3)
        )

    "trace loop path" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        cycleGraph.traceThenPaths(Set(c1)).await mustBe (
          Set(Path.Loop(pathWalk(Then(c1, c2), Then(c2, c1)))),
          Set(c1, c2)
        )

    "trace noose path" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        nooseGraph.traceThenPaths(Set(c1)).await mustBe (
          Set(
            Path.Noose(pathWalk(Then(c1, c2), Then(c2, c3), Then(c3, c3))),
            Path.Noose(pathWalk(Then(c1, c2), Then(c2, c3), Then(c3, c2)))
          ),
          Set(c1, c2, c3)
        )

  "GraphStructure.traceThenCyclesPaths" should:
    "trace cycles as direct paths" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        nooseGraph.traceThenCyclesPaths(visited = Set(c1), acc = Set()).await mustBe Set(
          Path.Loop(pathWalk(Then(c2, c3), Then(c3, c2))),
          Path.Noose(pathWalk(Then(c2, c3), Then(c3, c3)))
        )
