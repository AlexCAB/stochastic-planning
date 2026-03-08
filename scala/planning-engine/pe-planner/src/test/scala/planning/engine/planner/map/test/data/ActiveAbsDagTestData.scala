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
| created: 2026-03-08 |||||||||||*/



package planning.engine.planner.map.test.data

import cats.effect.IO
import planning.engine.common.graph.GraphStructure
import planning.engine.common.graph.edges.EdgeKey.Then
import planning.engine.common.graph.edges.EdgeKeySet
import planning.engine.planner.map.data.ActiveAbsDag
import planning.engine.common.values.node.MnId

trait ActiveAbsDagTestData extends DcgGraphTestData:
  lazy val mn11 = makeConDcgNode(id = MnId.Con(11))
  lazy val mn12 = makeConDcgNode(id = MnId.Con(12))

  lazy val linkEdges = dcgEdges.filter(_.key.isLink)

  lazy val dcgGraph = graphWithNodes.copy(
    nodes = graphWithNodes.nodes ++ List(mn11, mn12).map(n => n.id -> n).toMap,
    edges = linkEdges.map(e => e.key -> e).toMap,
    samples = sampleData.map(s => s.id -> s).toMap,
    structure = GraphStructure[IO](linkEdges.map(_.key).toSet)
  )

  lazy val backwordKeys = EdgeKeySet[Then](
    Then(mn11.id, mnId1),
    Then(mn12.id, mnId5),
    Then(mnId5, mnId5) // Loop edge also is valid
  )

  lazy val activeAbsDag = new ActiveAbsDag[IO](backwordKeys, dcgGraph)