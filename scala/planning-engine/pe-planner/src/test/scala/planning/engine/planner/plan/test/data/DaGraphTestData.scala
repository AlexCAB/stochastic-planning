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
  def makeConPnId(mnId: MnId.Con, count: Long = 0L): PnId.Con = PnId.Con(mnId, count)
  def makeConPnId(mnId: Long): PnId.Con = makeConPnId(MnId.Con(mnId))

  def makeAbsPnId(mnId: MnId.Abs, count: Long = 0L): PnId.Abs = PnId.Abs(mnId, count)
  def makeAbsPnId(mnId: Long): PnId.Abs = makeAbsPnId(MnId.Abs(mnId))

  def makeDagNode(id: PnId, time: Option[IoTime] = None, name: Option[String] = None): DagNode[IO] = new DagNode[IO](
    id = id,
    time = time,
    dcgNode = id.mnId match
      case mnId: MnId.Con => makeConDcgNode(id = mnId.value, name = name)
      case mnId: MnId.Abs => makeAbsDcgNode(id = mnId.value, name = name)
  )

  lazy val pnId1 = makeConPnId(mnId1, count = 1)
  lazy val pnId2 = makeConPnId(mnId2, count = 2)

  lazy val pnId3 = makeAbsPnId(mnId3, count = 1)
  lazy val pnId4 = makeAbsPnId(mnId4, count = 2)
  lazy val pnId5 = makeAbsPnId(mnId5, count = 3)
  lazy val pnId6 = makeAbsPnId(mnId6, count = 4)

  lazy val allConPnIds: List[PnId.Con] = List(pnId1, pnId2)
  lazy val allAbsPnIds: List[PnId.Abs] = List(pnId3, pnId4, pnId5, pnId6)

  lazy val allConDagNodes: List[DagNode[IO]] = allConPnIds.map(id => makeDagNode(id = id))
  lazy val allAbsDagNodes: List[DagNode[IO]] = allAbsPnIds.map(id => makeDagNode(id = id))

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

  lazy val allDagNodes = allConDagNodes ++ allAbsDagNodes
  lazy val allDagEdges = dagEdgesLink ++ dagEdgesThen
  
  // The structure of simpleDaGraph is:
  //
  //  [pnId1] ───then───▶ [pnId2]
  //     │                   │
  //     │link               │link
  //     ▼                   ▼
  //  (pnId3) ───then───▶ (pnId4) ───then───▶ (pnId5)
  //     │
  //     │link
  //     ▼
  //  (pnId6)
  //
  lazy val simpleDaGraph = makeDaGraph(allDagNodes, allDagEdges)
