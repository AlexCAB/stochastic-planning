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

package planning.engine.planner.mpi.actors.node.data.state

import cats.syntax.all.*
import cats.syntax.ext.*
import planning.engine.common.enums.EdgeType
import planning.engine.common.errors.*
import planning.engine.common.values.node.{HnIndex, MnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.node.data.Lookup
import planning.engine.planner.mpi.model.data.edge.MeRef
import planning.engine.planner.mpi.model.data.samples.Sample
import planning.engine.planner.mpi.repr.Representable

private[node] final case class Struct(
    // Counter for generating unique HnIndex
    // NOTE: In practice SampleId can be used as HnIndex, since it also is unique and monotonically increasing,
    // NOTE: but for now we keep them separate for clarity and canonical math model correspondence.
    nextHnIndex: Long,

    // Map of incoming edges: previous source node -> this node
    incomingMap: Map[MnId, Struct.EdgeData],

    // Map of outgoing edges: this node -> next target node
    outgoingMap: Map[MnId, Struct.EdgeData],

    // Samples that include this node, along with their HnIndex and properties.
    // In more advanced implementation, sample data have to be sored in separate
    // sample data storage and this map can be a cache.
    sampleMap: Map[SampleId, Struct.SampleData],

    // Total number of samples in the map network (used for probs calculation).
    // In future also should come from separate sample data storage, but for now it is in Manager state.
    totalSamplesCount: Long,
) extends Representable:
  import Struct.*

  // TODO: Approximate inference algorithm:
  // TODO:   1. For each outgoingMap:
  // TODO:     1.1. reCalcSamples = edgeData.sampleIds intersect with InferenceMsg.activeSampleIds
  // TODO:     1.2. (probability, utility) = calculate based on reCalcSamples.map(_.values) and totalSamplesCount
  // TODO:   2. outgoingMap.filter(probability * utility > threshold).foreach(send InferenceMsg to next Node)

  private def upsertEdgeMap[F[_]: MT](
      edgeMap: Map[MnId, EdgeData],
      mnId: MnId,
      node: Node,
      sampleIds: Set[SampleId],
      et: EdgeType,
  ): F[Map[MnId, EdgeData]] = edgeMap.get(mnId) match
    case Some(EdgeData(n, sIs, t)) if n == node && t == et =>
      (edgeMap + (mnId -> EdgeData(n, sIs ++ sampleIds, t))).pure // If edge already exists, just update sample IDs.

    case Some(EdgeData(n, _, t)) if n == node =>
      s"Edge type mismatch: expected $t, got $et, for edge $mnId".assertionError

    case Some(EdgeData(n, _, _)) => s"Edge reference mismatch: expected ${n.mnId}, got ${node.mnId}".assertionError
    case None => (edgeMap + (mnId -> EdgeData(node, sampleIds, et))).pure // If edge does not exist, create a new one.

  private def upsertSampleMap[F[_]: MT](
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
  def upsertEdgeSrc[F[_]: MT](meRef: MeRef, props: Map[SampleId, Sample.Props]): F[Struct] =
    for
      _ <- meRef.key.trg.assertEquals(meRef.trgNode.mnId, "Edge target node does not match meRef target")
      newOutgoing <- upsertEdgeMap(outgoingMap, meRef.key.trg, meRef.trgNode, props.keySet, meRef.key.asEdgeType)
      (newSampleMap, newNextHnIndex) <- upsertSampleMap(props)
    yield this.copy(
      nextHnIndex = newNextHnIndex,
      outgoingMap = newOutgoing,
      sampleMap = newSampleMap,
    )

  // Edge target (meRef.key.trg) is this node, source is previous neighbor node.
  // Update incomingMap with new edge data.
  def upsertEdgeTrg[F[_]: MT](meRef: MeRef, props: Map[SampleId, Sample.Props]): F[Struct] =
    for
      _ <- meRef.key.src.assertEquals(meRef.srcNode.mnId, "Edge source node does not match meRef source")
      newIncoming <- upsertEdgeMap(incomingMap, meRef.key.src, meRef.srcNode, props.keySet, meRef.key.asEdgeType)
      (newSampleMap, newNextHnIndex) <- upsertSampleMap(props)
    yield this.copy(
      nextHnIndex = newNextHnIndex,
      incomingMap = newIncoming,
      sampleMap = newSampleMap,
    )

  def withTotalSamplesCount[F[_]: MT](count: Long): F[Struct] = this.copy(totalSamplesCount = count).pure

  private def buildMap(m: Map[MnId, EdgeData], p: EdgeType => Boolean): Map[SampleId, Set[Node]] = m
    .values.filter(d => p(d.et))
    .flatMap(d => d.sampleIds.map(_ -> d.neighbor))
    .groupMap((sId, _) => sId)((_, neighbor) => neighbor)
    .view.mapValues(_.toSet).toMap

  lazy val lookup: Lookup = Lookup(
    inLink = buildMap(incomingMap, _.isLink),
    outLink = buildMap(outgoingMap, _.isLink),
    inThen = buildMap(incomingMap, _.isThen),
    outThen = buildMap(outgoingMap, _.isThen),
  )

private[node] object Struct:
  final case class EdgeData(neighbor: Node, sampleIds: Set[SampleId], et: EdgeType)
  final case class SampleData(index: HnIndex, props: Sample.Props)

  val init = Struct(1L, Map.empty, Map.empty, Map.empty, 0L)
