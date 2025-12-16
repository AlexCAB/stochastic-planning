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
| created: 2025-12-15 |||||||||||*/

package planning.engine.planner.map

import cats.effect.IO
import cats.syntax.all.*
import cats.effect.unsafe.IORuntime
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.common.values.sample.SampleId
import planning.engine.map.hidden.edge.HiddenEdge
import planning.engine.map.hidden.edge.HiddenEdge.SampleIndexies
import planning.engine.map.hidden.node.ConcreteNode
import planning.engine.map.io.node.InputNode
import planning.engine.map.io.variable.BooleanIoVariable
import planning.engine.map.samples.sample.SampleData
import planning.engine.map.subgraph.MapSubGraph
import planning.engine.planner.map.dcg.edges.DcgEdge
import planning.engine.planner.map.dcg.nodes.ConcreteDcgNode
import planning.engine.planner.map.dcg.state.DcgState

trait SimpleMemStateTestData:
  private implicit lazy val ioRuntime: IORuntime = IORuntime.global

  lazy val hnId1 = HnId(1)
  lazy val hnId2 = HnId(2)
  lazy val sampleId1 = SampleId(1001)
  lazy val sampleId2 = SampleId(1002)
  lazy val sampleInd1 = SampleIndexies(sampleId1, HnIndex(201), HnIndex(202))
  lazy val sampleInd2 = SampleIndexies(sampleId2, HnIndex(203), HnIndex(204))
  lazy val boolInNode = InputNode[IO](IoName("boolInNode"), BooleanIoVariable[IO](Set(true, false)))
  lazy val conNode = ConcreteNode[IO](HnId(0), None, None, boolInNode, IoIndex(0))
  def sampleData(id: SampleId) = SampleData(id, 10L, 5.0, None, None)

  lazy val mapSubGraph = MapSubGraph[IO](
    concreteNodes = List(
      conNode.copy(id = hnId1, valueIndex = IoIndex(101L)),
      conNode.copy(id = hnId2, valueIndex = IoIndex(102L))
    ),
    abstractNodes = List(),
    edges = List(
      HiddenEdge(edgeType = EdgeType.LINK, sourceId = hnId1, targetId = hnId2, samples = List(sampleInd1)),
      HiddenEdge(edgeType = EdgeType.THEN, sourceId = hnId2, targetId = hnId1, samples = List(sampleInd2))
    ),
    skippedSamples = List(),
    loadedSamples = List(sampleData(sampleId1), sampleData(sampleId2))
  )

  lazy val ioValues = mapSubGraph.concreteNodes.map(_.ioValue)

  lazy val dcgNodes = mapSubGraph.concreteNodes
    .traverse(ConcreteDcgNode.apply).unsafeRunSync()
    .map(n => n.ioValue -> Set(n)).toMap

  lazy val dcgEdges = mapSubGraph.edges.traverse(DcgEdge.apply[IO]).unsafeRunSync()

  private def references(tp: EdgeType, isForward: Boolean): Map[HnId, Set[HnId]] = dcgEdges
    .map(_.key).filter(_.edgeType == tp)
    .groupBy(k => if isForward then k.sourceId else k.targetId)
    .view.mapValues(_.map(k => if isForward then k.targetId else k.sourceId).toSet)
    .toMap

  lazy val dcgState = DcgState[IO](
    ioValues = dcgNodes.map((k, ns) => k -> ns.map(_.id)),
    concreteNodes = dcgNodes.flatMap((_, ns) => ns.map(n => n.id -> n)),
    abstractNodes = Map.empty,
    edges = dcgEdges.map(e => e.key -> e).toMap,
    forwardLinks = references(EdgeType.LINK, isForward = true),
    backwardLinks = references(EdgeType.LINK, isForward = false),
    forwardThen = references(EdgeType.THEN, isForward = true),
    backwardThen = references(EdgeType.THEN, isForward = false),
    samplesData = mapSubGraph.loadedSamples.map(s => s.id -> s).toMap
  )
