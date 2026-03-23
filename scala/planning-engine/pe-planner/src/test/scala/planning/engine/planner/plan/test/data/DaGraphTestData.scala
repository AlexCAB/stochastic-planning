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
import planning.engine.common.graph.edges.{IndexMap, PeKey}
import planning.engine.common.values.io.IoTime
import planning.engine.common.values.node.{MnId, PnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.map.test.data.{DcgEdgeTestData, DcgNodeTestData}
import planning.engine.planner.plan.dag.DaGraph
import planning.engine.planner.plan.dag.edges.DagEdge
import planning.engine.planner.plan.dag.nodes.DagNode

trait DaGraphTestData extends DcgNodeTestData with DcgEdgeTestData:
  def makeConPnId(mnId: MnId.Con, time: Long = 0L): PnId.Con = PnId.Con(mnId, IoTime(time))
  def makeAbsPnId(mnId: MnId.Abs, time: Long = 0L): PnId.Abs = PnId.Abs(mnId, IoTime(time))

  def makeDagNode(id: PnId, name: Option[String] = None): DagNode[IO] = DagNode[IO](
    id,
    id.mnId match
      case mnId: MnId.Con => makeConDcgNode(id = mnId.value, name = name)
      case mnId: MnId.Abs => makeAbsDcgNode(id = mnId.value, name = name)
  )

  lazy val pnId1 = makeConPnId(mnId1, time = 1)
  lazy val pnId2 = makeConPnId(mnId2, time = 2)

  lazy val pnId3 = makeAbsPnId(mnId3, time = 1)
  lazy val pnId4 = makeAbsPnId(mnId4, time = 2)
  lazy val pnId5 = makeAbsPnId(mnId5, time = 3)
  lazy val pnId6 = makeAbsPnId(mnId6, time = 4)

  lazy val allConPnIds: Set[PnId.Con] = Set(pnId1, pnId2)
  lazy val allAbsPnIds: Set[PnId.Abs] = Set(pnId3, pnId4, pnId5)

  lazy val allConDagNodes: List[DagNode[IO]] = List(pnId1, pnId2).map(id => makeDagNode(id = id))
  lazy val allAbsDagNodes: List[DagNode[IO]] = List(pnId3, pnId4, pnId5).map(id => makeDagNode(id = id))

  def makeDagEdgeLink(src: PnId, trg: PnId, samples: Iterable[(SampleId, IndexMap)] = Iterable.empty): DagEdge[IO] =
    new DagEdge[IO](PeKey.Link(src, trg), makeDcgEdgeLink(src.mnId, trg.mnId, samples))

  def makeDagEdgeThen(src: PnId, trg: PnId, samples: Iterable[(SampleId, IndexMap)] = Iterable.empty): DagEdge[IO] =
    new DagEdge[IO](PeKey.Then(src, trg), makeDcgEdgeThen(src.mnId, trg.mnId, samples))

  lazy val dagEdgesLink = List(
    makeDagEdgeLink(pnId1, pnId3), 
    makeDagEdgeLink(pnId3, pnId6),
    makeDagEdgeLink(pnId2, pnId4)
  )

  lazy val dagEdgesThen = List(
    makeDagEdgeThen(pnId1, pnId2),
    makeDagEdgeThen(pnId3, pnId4),
    makeDagEdgeThen(pnId4, pnId5)
  )

  def makeDaGraph(nodes: Iterable[DagNode[IO]], edges: Iterable[DagEdge[IO]]): DaGraph[IO] =
    new DaGraph[IO](nodes = nodes.map(n => n.id -> n).toMap, edges = edges.map(e => e.key -> e).toMap)

  lazy val simpleDaGraph = makeDaGraph(allConDagNodes ++ allAbsDagNodes, dagEdgesLink ++ dagEdgesThen)
