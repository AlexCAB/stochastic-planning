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
| created: 2026-01-18 |||||||||||*/

package planning.engine.planner.map.data

import cats.effect.IO
import planning.engine.common.UnitSpecWithData
import planning.engine.common.validation.ValidationCheck
import planning.engine.common.graph.edges.EdgeKey.Then
import planning.engine.common.values.node.MnId
import planning.engine.planner.map.test.data.ActiveAbsDagTestData

class ActiveAbsDagSpec extends UnitSpecWithData with ValidationCheck:

  private class CaseData extends Case with ActiveAbsDagTestData:
    lazy val testCn1 = makeConDcgNode(id = MnId.Con(997))
    lazy val testCn2 = makeConDcgNode(id = MnId.Con(998))
    lazy val testAn2 = makeAbsDcgNode(id = MnId.Abs(999))

  "ActiveAbsDag.apply(EdgeKeySet, DcgGraph)" should:
    "create instance from valid data" in newCase[CaseData]: (tn, data) =>
      import data.{activeAbsDag, backwordKeys, dcgGraph}

      ActiveAbsDag[IO](dcgGraph.nodes.values, dcgGraph.edges.values, backwordKeys.keys, dcgGraph.samples.values)
        .logValue(tn).asserting(_ mustBe activeAbsDag)

    "fail if in linkEdges THEN edge found" in newCase[CaseData]: (tn, data) =>
      import data.{dcgGraph, backwordKeys, dcgEdges}

      ActiveAbsDag[IO](dcgGraph.nodes.values, dcgEdges, backwordKeys.keys, dcgGraph.samples.values)
        .logValue(tn).assertThrowsError(_.getMessage must include("Only LINK edges can be added"))

    "fail if back THEN edges refer to unknown HnIds" in newCase[CaseData]: (tn, data) =>
      import data.*
      val invalidKeys = backwordKeys.keys + Then(testCn1.id, testCn2.id)

      ActiveAbsDag[IO](dcgGraph.nodes.values, dcgGraph.edges.values, invalidKeys, dcgGraph.samples.values)
        .logValue(tn).assertThrowsError(_.getMessage must include("Back THEN edges refer to unknown HnIds"))

    "fail if target of back THEN edges are not connected to active graph" in newCase[CaseData]: (tn, data) =>
      import data.{dcgGraph, backwordKeys, mn11}
      val invalidKeys = backwordKeys.keys + Then(mn11.id, mn11.id)

      ActiveAbsDag[IO](dcgGraph.nodes.values, dcgGraph.edges.values, invalidKeys, dcgGraph.samples.values)
        .logValue(tn).assertThrowsError(_.getMessage must include("Target do not connected to active graph"))

    "fail if some nodes are not connected to any concrete nodes" in newCase[CaseData]: (tn, data) =>
      import data.{dcgGraph, backwordKeys, testAn2}
      val invalidNodes = dcgGraph.nodes.values.toList :+ testAn2

      ActiveAbsDag[IO](invalidNodes, dcgGraph.edges.values, backwordKeys.keys, dcgGraph.samples.values)
        .logValue(tn).assertThrowsError(_.getMessage must include("Some nodes are not connected to any concrete"))
