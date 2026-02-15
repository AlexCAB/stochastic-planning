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
| created: 2025-12-06 |||||||||||*/

package planning.engine.map.subgraph

import cats.MonadThrow
import planning.engine.common.validation.Validation
import planning.engine.common.values.io.IoValue
import planning.engine.map.hidden.edge.HiddenEdge
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import planning.engine.map.samples.sample.SampleData
import planning.engine.common.values.sample.SampleId

final case class MapSubGraph[F[_]: MonadThrow](
    concreteNodes: List[ConcreteNode[F]],
    abstractNodes: List[AbstractNode[F]],
    edges: List[HiddenEdge],
    loadedSamples: List[SampleData],
    skippedSamples: List[SampleId]
) extends Validation:
  lazy val allIoValues: Set[IoValue] = concreteNodes.map(_.ioValue).toSet
  lazy val allSampleIds: Set[SampleId] = loadedSamples.map(_.id).toSet ++ skippedSamples.toSet

  override lazy val validations: (String, List[Throwable]) =
    val conIds = concreteNodes.map(_.id)
    val absIds = abstractNodes.map(_.id)
    val allIds = conIds ++ absIds
    val allEdgeHdIds = edges.flatMap(e => List(e.sourceId, e.targetId))
    val edgeKeys = edges.map(e => (e.edgeType, e.sourceId, e.targetId))
    val loadedSamplesIsd = loadedSamples.map(_.id)
    val allSamplesIds = loadedSamplesIsd ++ skippedSamples
    val allEdgeSamplesIds = edges.flatMap(_.samples.map(_.sampleId)).distinct

    validate(s"MapSubGraph(nodes=${(concreteNodes ++ abstractNodes).map(_.id)})")(
      conIds.isDistinct("Concrete node IDs must be distinct"),
      absIds.isDistinct("Abstract node IDs must be distinct"),
      allIds.isDistinct("Node IDs must be distinct"),
      edgeKeys.isDistinct("Edge keys (type, sourceId, targetId) must be distinct"),
      allIds.containsAllOf(allEdgeHdIds, "All Edge node IDs must exist in concrete or abstract nodes list"),
      allSamplesIds.isDistinct("Sample IDs must be distinct"),
      allSamplesIds.haveSameElems(allEdgeSamplesIds, "All edge sample IDs must exist in loaded or skipped samples")
    )

object MapSubGraph:
  def emptySubGraph[F[_]: MonadThrow]: MapSubGraph[F] = MapSubGraph[F](
    concreteNodes = List.empty,
    abstractNodes = List.empty,
    edges = List.empty,
    loadedSamples = List.empty,
    skippedSamples = List.empty
  )
