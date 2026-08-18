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
| created: 09.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.node.data

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.errors.*
import planning.engine.common.values.node.{HnIndex, MnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.node.logic.Actor
import planning.engine.planner.mpi.common.data.edge.MeRef
import planning.engine.planner.mpi.common.data.samples.Sample
import planning.engine.planner.mpi.common.repr.Representable

private[node] final case class State(
    // Counter for generating unique HnIndex
    nextHnIndex: Long,

    // Map of incoming edges: previous source node -> this node
    incomingMap: Map[MnId, State.EdgeData],

    // Map of outgoing edges: this node -> next target node
    outgoingMap: Map[MnId, State.EdgeData],

    // Samples that include this node, along with their HnIndex and properties.
    // In more advanced implementation, sample data have to be sored in separate
    // sample data storage and this map can be a cache.
    sampleMap: Map[SampleId, State.SampleData],

    // Total number of samples in the map network (used for probs calculation).
    // In future also should come from separate sample data storage, but for now it is in Manager state.
    totalSamplesCount: Long,
) extends Representable:
  import State.*

  // TODO: Approximate inference algorithm:
  // TODO:   1. For each outgoingMap:
  // TODO:     1.1. reCalcSamples = edgeData.sampleIds intersect with InferenceMsg.activeSampleIds
  // TODO:     1.2. (probability, utility) = calculate based on reCalcSamples.map(_.values) and totalSamplesCount
  // TODO:   2. outgoingMap.filter(probability * utility > threshold).foreach(send InferenceMsg to next Node)

  private def upsertEdgeMap[F[_]: MonadThrow](
      edgeMap: Map[MnId, EdgeData],
      key: MnId,
      node: Node,
      sampleIds: Set[SampleId],
  ): F[Map[MnId, EdgeData]] = edgeMap.get(key) match
    case Some(EdgeData(n, sIs)) if n == node => (edgeMap + (key -> EdgeData(n, sIs ++ sampleIds))).pure
    case Some(EdgeData(n, _)) => s"Edge target reference mismatch: expected ${n.mnId}, got ${node.mnId}".assertionError
    case None                 => (edgeMap + (key -> EdgeData(node, sampleIds))).pure

  private def upsertSampleMap[F[_]: MonadThrow](
      props: Map[SampleId, Sample.Props],
  ): F[(Map[SampleId, SampleData], Long)] = props.toList.foldLeftM((sampleMap, nextHnIndex)):
    case ((samples, nextId), (sId, p)) if samples.contains(sId) && samples(sId).props == p =>
      (samples, nextId).pure // This sample added before, no need to update
    case ((samples, nextId), (sId, p)) if samples.contains(sId) =>
      s"Sample $sId already exists with different properties: ${samples(sId).props} vs $p".assertionError
    case ((samples, nextId), (sId, p)) =>
      (samples + (sId -> SampleData(HnIndex(nextId), p)), nextId + 1).pure // Add new sample with next HnIndex

  // Edge source (meRef.key.src) is this node, target is next neighbor node.
  // Update outgoingMap with new edge data.
  def upsertEdgeSrc[F[_]: MonadThrow](
      meRef: MeRef,
      props: Map[SampleId, Sample.Props],
  )(using d: Actor.Def, c: Actor.Ctx): F[State] =
    for
      _ <- meRef.key.src.assertEquals(d.id, "Edge source does not match this node's ID")
      _ <- meRef.srcNode.assertEquals(d.self, "Edge source node does not match this node")
      newOutgoing <- upsertEdgeMap(outgoingMap, meRef.key.trg, meRef.trgNode, props.keySet)
      (newSampleMap, newNextHnIndex) <- upsertSampleMap(props)
    yield this.copy(
      nextHnIndex = newNextHnIndex,
      outgoingMap = newOutgoing,
      sampleMap = newSampleMap,
    )

  // Edge target (meRef.key.trg) is this node, source is previous neighbor node.
  // Update incomingMap with new edge data.
  def upsertEdgeTrg[F[_]: MonadThrow](
      meRef: MeRef,
      props: Map[SampleId, Sample.Props],
  )(using d: Actor.Def, c: Actor.Ctx): F[State] =
    for
      _ <- meRef.key.trg.assertEquals(d.id, "Edge target does not match this node's ID")
      _ <- meRef.trgNode.assertEquals(d.self, "Edge target node does not match this node")
      newIncoming <- upsertEdgeMap(incomingMap, meRef.key.src, meRef.srcNode, props.keySet)
      (newSampleMap, newNextHnIndex) <- upsertSampleMap(props)
    yield this.copy(
      nextHnIndex = newNextHnIndex,
      incomingMap = newIncoming,
      sampleMap = newSampleMap,
    )

  def withTotalSamplesCount[F[_]: MonadThrow](count: Long): F[State] = this.copy(totalSamplesCount = count).pure

private[node] object State:
  final case class EdgeData(neighbor: Node, sampleIds: Set[SampleId])
  final case class SampleData(index: HnIndex, props: Sample.Props)

  val init = State(1L, Map.empty, Map.empty, Map.empty, 0L)
