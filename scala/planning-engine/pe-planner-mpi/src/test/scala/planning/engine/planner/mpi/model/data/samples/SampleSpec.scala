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
| created: 2026-10-26 |||||||||||*/

package planning.engine.planner.mpi.model.data.samples

import cats.effect.IO
import planning.engine.common.UnitSpecWithData
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.test.data.{EdgeTestData, NodeTestData}

class SampleSpec extends UnitSpecWithData with NodeTestData with EdgeTestData:
  private class CaseData extends Case with WithMapNode with WithMapEdge:
    lazy val sampleId: SampleId = SampleId(1L)
    lazy val conNode: Node = makeNodeStub(conMnId, "con-node")
    lazy val absNode: Node = makeNodeStub(absMnId, "abs-node")
    lazy val edge: MeKey = MeKey.Link(conMnId, absMnId)

  "Sample.apply(...)" should:
    "build a sample from a manually defined sample, with info and nodes map" in newCase[CaseData]: (_, data) =>
      import data.*
      val raw = manSample(edge)

      Sample[IO](sampleId, raw, Set(conNode, absNode)).asserting(_ mustBe Sample(
        id = sampleId,
        props = raw.props,
        info = Some(raw.info),
        edges = Set(edge),
        nodes = Map(conMnId -> conNode.data, absMnId -> absNode.data),
      ))

    "build a sample from a generated sample, without info" in newCase[CaseData]: (_, data) =>
      import data.*
      val raw = genSample(edge)

      Sample[IO](sampleId, raw, Set(conNode, absNode)).asserting(_ mustBe Sample(
        id = sampleId,
        props = raw.props,
        info = None,
        edges = Set(edge),
        nodes = Map(conMnId -> conNode.data, absMnId -> absNode.data),
      ))

    "raise an error when a node used in the sample edges is not given" in newCase[CaseData]: (_, data) =>
      import data.*
      Sample[IO](sampleId, manSample(edge), Set(conNode))
        .assertThrowsError[AssertionError](_.getMessage must include("has edges with MnIds not found in given nodes"))

    "raise an error when sample edges contain Nim ids" in newCase[CaseData]: (_, data) =>
      import data.*
      Sample[IO](sampleId, genSample(MeKey.Link(conMnId, nim2)), Set(conNode, absNode))
        .assertThrowsError[AssertionError](_.getMessage must include("has edges with MnIds not found in given nodes"))
