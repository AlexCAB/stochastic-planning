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
| created: 2025-04-07 |||||||||||*/

package planning.engine.map.samples.sample

import cats.MonadThrow
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.common.values.sample.SampleId
import planning.engine.common.errors.{assertionError, assertSameSize, assertDistinct}
import neotypes.model.types.{Relationship, Value}
import cats.syntax.all.*
import planning.engine.common.properties.*

final case class SampleEdge(
    sourceValue: HnIndex,
    targetValue: HnIndex,
    edgeType: EdgeType,
    sampleId: SampleId
):
  override def toString: String = s"SampleEdge($$sourceValue -- $sampleId($edgeType) -> $targetValue)"

object SampleEdge:
  final case class New(
      source: HnId,
      target: HnId,
      edgeType: EdgeType
  ):

    def toQueryParams[F[_]: MonadThrow](sampleId: SampleId, indexies: Map[HnId, HnIndex]): F[(String, List[Long])] =
      def getIndex(hnId: HnId): F[HnIndex] = indexies.get(hnId) match
        case Some(id) => id.pure
        case _        => s"Missing HnIndex for $hnId in $indexies".assertionError

      for
        sourceValue <- getIndex(source)
        targetValue <- getIndex(target)
      yield (sampleId.toPropName, List(sourceValue.value, targetValue.value))

    override def toString: String = s"ObservedEdge($source --$edgeType-> target=$target)"

  def fromEdgeBySampleId[F[_]: MonadThrow](
      edge: Relationship,
      sampleId: SampleId
  ): F[SampleEdge] =
    def find: F[(HnIndex, HnIndex)] = edge.getList[F, Long](sampleId.toPropName).flatMap:
      case sv :: tv :: Nil => (HnIndex(sv), HnIndex(tv)).pure
      case _               => s"Edge $edge does not contain properties for sampleId = $sampleId".assertionError

    for
      (sourceValue, targetValue) <- find
      edgeType <- EdgeType.fromLabel(edge.relationshipType).fold(_.assertionError, _.pure)
    yield SampleEdge(
      sourceValue = sourceValue,
      targetValue = targetValue,
      edgeType = edgeType,
      sampleId = sampleId
    )

  def fromEdge[F[_]: MonadThrow](edge: Relationship): F[Set[SampleEdge]] =
    def parse(sIdStr: String, values: Value): F[(SampleId, (HnIndex, HnIndex))] =
      for
        sampleId <- SampleId.fromPropName(sIdStr)
        (sourceValue, targetValue) <- values match
          case Value.ListValue(Value.Integer(sv) :: Value.Integer(tv) :: Nil) => (HnIndex(sv), HnIndex(tv)).pure
          case _ => s"Expected a list of two Long values for sampleId $sampleId, but got: $values".assertionError
      yield (sampleId, (sourceValue, targetValue))

    for
      edgeType <- EdgeType.fromLabel(edge.relationshipType).fold(_.assertionError, _.pure)
      samples <- edge.properties.toList.traverse((sId, values) => parse(sId, values))
      sampleSet = samples.toSet
      _ <- (samples, sampleSet).assertSameSize("Fond duplicate sample IDs in edge properties")
      _ <- samples.map(_._1).assertDistinct("Sample edge can't be included to the same map edge multiple times")
    yield sampleSet.map((sId, vals) =>
      SampleEdge(
        sourceValue = vals._1,
        targetValue = vals._2,
        edgeType = edgeType,
        sampleId = sId
      )
    )
