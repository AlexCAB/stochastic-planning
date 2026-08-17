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
import planning.engine.planner.mpi.common.data.edge.{EdgeData, MeRef}
import planning.engine.planner.mpi.common.data.samples.Sample
import planning.engine.planner.mpi.common.repr.Representable

private[node] final case class State(
    // Counter for generating unique HnIndex
    nextHnIndex: Long,

    // Map of incoming edges: previous source node -> this node
    incomingMap: Map[MnId, EdgeData],

    // Map of outgoing edges: this node -> next target node
    outgoingMap: Map[MnId, EdgeData],

    // Samples that include this node, along with their HnIndex and properties.
    // In more advanced implementation, sample data have to be sored in separate
    // sample data storage and this map can be a cache.
    sampleMap: Map[SampleId, State.SampleData],

    // Total number of samples in the map network (used for probs calculation).
    // In future also should come from separate sample data storage, but for now it is in Manager state.
    totalSamplesCount: Long,
) extends Representable:

  // TODO: Approximate inference algorithm:
  // TODO:   1. For each outgoingMap:
  // TODO:     1.1. reCalcSamples = edgeData.sampleIds intersect with InferenceMsg.activeSampleIds
  // TODO:     1.2. (probability, utility) = calculate based on reCalcSamples.map(_.values) and totalSamplesCount
  // TODO:   2. outgoingMap.filter(probability * utility > threshold).foreach(send InferenceMsg to next Node)

  // Edge source (meRef.key.src) is this node, target is next neighbor node.
  // Update outgoingMap with new edge data.
  def upsertEdgeSrc[F[_]: MonadThrow](meRef: MeRef, props: Map[SampleId, Sample.Props]): F[State] = ???
  // TODO: Validate meRef.key.src == this node's MnId, meRef.srcNode == this node
  // TODO: If outgoingMap have meRef.key.trg, then update sampleIds. Else add new entry.
  // TODO: For each sampleId in props:
  // TODO:   If sampleId exists in sampleMap, then validate it have same props values.
  // TODO:   If not exists, then create new HnIndex and add to sampleMap with props.

  //
//    def joinedData(newRef: MeRef, newData: EdgeData): F[EdgeData] = outgoingMap.get(newRef.key.trg) match
//      case Some((trgRef, data)) if trgRef == newRef.trg => data.join(newData)
//      case Some((trgRef, _)) => s"Edge target reference mismatch: expected $trgRef, got ${newRef.trg}".assertionError
//      case None              => newData.pure
//
//    joinedData(newRef, newData).map(data => copy(outgoingMap = outgoingMap + (newRef.key.trg -> (newRef.trg, data))))

  // Edge target (meRef.key.trg) is this node, source is previous neighbor node.
  // Update incomingMap with new edge data.
  def upsertEdgeTrg[F[_]: MonadThrow](meRef: MeRef, props: Map[SampleId, Sample.Props]): F[State] = ???
  // TODO: Same as upsertEdgeSrc, but for incomingMap.
  // TODO: Validate meRef.key.trg == this node's MnId, meRef.trgNode == this node.

//    incomingMap.get(newRef.key.src) match
//    case Some(ref) if ref == newRef.src => this.pure // Edge already exists, no change needed
//    case Some(ref) => s"Ref not match for ${newRef.key.src}: expected $ref, got ${newRef.src}".assertionError
//    case None      => copy(incomingMap = incomingMap + (newRef.key.src -> newRef.src)).pure

  def withTotalSamplesCount[F[_]: MonadThrow](count: Long): F[State] = ???

private[node] object State:
  final case class EdgeData(neighbor: Node, sampleIds: Set[SampleId])
  final case class SampleData(index: HnIndex, props: Sample.Props)

  val init = State(1L, Map.empty, Map.empty, Map.empty, 0L)
