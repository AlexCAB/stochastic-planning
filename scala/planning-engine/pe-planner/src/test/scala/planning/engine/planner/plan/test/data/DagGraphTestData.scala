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
| created: 2026-03-13 |||||||||||*/

package planning.engine.planner.plan.test.data

import cats.effect.IO
import planning.engine.common.graph.edges.IndexMap
import planning.engine.common.values.io.IoTime
import planning.engine.common.values.node.{MnId, SnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.map.dcg.edges.DcgEdge
import planning.engine.planner.map.test.data.{DcgEdgeTestData, DcgNodeTestData}
import planning.engine.planner.plan.dag.DagGraph
import planning.engine.planner.plan.dag.edges.DagEdge
import planning.engine.planner.plan.dag.nodes.DagNode

trait DagGraphTestData extends DcgNodeTestData with DcgEdgeTestData:
  def makeConSnId(mnId: MnId.Con, time: Long = 0L): SnId = SnId(mnId, IoTime(time))
  def makeAbsSnId(mnId: MnId.Abs, time: Long = 0L): SnId = SnId(mnId, IoTime(time))

  def makeDagNode(id: SnId, name: Option[String] = None): DagNode[IO] = DagNode[IO](
    id,
    id.mnId match
      case mnId: MnId.Con => makeConDcgNode(id = mnId.value, name = name)
      case mnId: MnId.Abs => makeAbsDcgNode(id = mnId.value, name = name)
  )

  lazy val snId1 = makeConSnId(mnId1, time = 1)
  lazy val snId2 = makeConSnId(mnId2, time = 2)

  lazy val snId3 = makeAbsSnId(mnId3, time = 1)
  lazy val snId4 = makeAbsSnId(mnId4, time = 2)
  lazy val snId5 = makeAbsSnId(mnId5, time = 3)

  lazy val allConSnId: Set[SnId] = Set(snId1, snId2)
  lazy val allAbsSnId: Set[SnId] = Set(snId3, snId4, snId5)

  lazy val conDagNodes: List[DagNode[IO]] = List(snId1, snId2).map(id => makeDagNode(id = id))
  lazy val absDagNodes: List[DagNode[IO]] = List(snId3, snId4, snId5).map(id => makeDagNode(id = id))

  def makeDagEdge(src: SnId, trg: SnId, dcgEdge: DcgEdge[IO]): DagEdge[IO] = DagEdge[IO](DagEdge.Key(src, trg), dcgEdge)

  def makeDagEdgeLink(src: SnId, trg: SnId, samples: Iterable[(SampleId, IndexMap)] = Iterable.empty): DagEdge[IO] =
    makeDagEdge(src, trg, makeDcgEdgeLink(src.mnId, trg.mnId, samples))

  def makeDagEdgeThen(src: SnId, trg: SnId, samples: Iterable[(SampleId, IndexMap)] = Iterable.empty): DagEdge[IO] =
    makeDagEdge(src, trg, makeDcgEdgeThen(src.mnId, trg.mnId, samples))

  lazy val dagEdgesLink = List(makeDagEdgeLink(snId1, snId3), makeDagEdgeLink(snId2, snId4))

  lazy val dagEdgesThen = List(
    makeDagEdgeThen(snId1, snId2),
    makeDagEdgeThen(snId3, snId4),
    makeDagEdgeThen(snId4, snId5)
  )

  def makeDagGraph(nodes: Iterable[DagNode[IO]], edges: Iterable[DagEdge[IO]]): DagGraph[IO] =
    new DagGraph[IO](nodes = nodes.map(n => n.id -> n).toMap, edges = edges.map(e => e.key -> e).toMap)

  lazy val simpleDagGraph = makeDagGraph(conDagNodes ++ absDagNodes, dagEdgesLink ++ dagEdgesThen)
