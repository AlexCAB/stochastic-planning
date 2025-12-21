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
import planning.engine.common.errors.{assertionError, assertDistinct, assertUniform}
import neotypes.model.types.{Relationship, Value}
import cats.syntax.all.*

final case class SampleEdge(
    source: SampleEdge.End,
    target: SampleEdge.End,
    edgeType: EdgeType,
    sampleId: SampleId
):
  override def toString: String = s"SampleEdge($source -- ${sampleId.value}:$edgeType -> $target)"

object SampleEdge:
  final case class End(hnId: HnId, value: HnIndex):
    override def toString: String = s"(hn = ${hnId.value}, value = ${value.value})"

  final case class New(source: HnId, target: HnId, edgeType: EdgeType):
    def toQueryParams[F[_]: MonadThrow](sampleId: SampleId, indexies: Map[HnId, HnIndex]): F[(String, List[Long])] =
      def getIndex(hnId: HnId): F[HnIndex] = indexies.get(hnId) match
        case Some(id) => id.pure
        case _        => s"Missing HnIndex for $hnId in $indexies".assertionError

      for
        sourceValue <- getIndex(source)
        targetValue <- getIndex(target)
      yield (sampleId.toPropName, List(sourceValue.value, targetValue.value))

    override def toString: String = s"ObservedEdge($source --$edgeType-> target=$target)"

  def fromEdgesBySampleId[F[_]: MonadThrow](
      edges: List[(HnId, Relationship, HnId)],
      sampleId: SampleId
  ): F[List[SampleEdge]] =
    val propName = sampleId.toPropName

    val builtEdges = edges.traverse((sourceHnId, edge, targetHnId) =>
      edge.properties.filter((k, _) => k.equalsIgnoreCase(propName)).toList match
        case (_, Value.ListValue(Value.Integer(sv) :: Value.Integer(tv) :: Nil)) :: Nil =>
          EdgeType.fromLabel(edge.relationshipType).fold(_.assertionError, _.pure).map(edgeType =>
            Some(SampleEdge(
              source = End(sourceHnId, HnIndex(sv)),
              target = End(targetHnId, HnIndex(tv)),
              edgeType = edgeType,
              sampleId = sampleId
            ))
          )
        case Nil => None.pure
        case props =>
          s"Expected exactly one sample property by key '$propName' with list value of two elements, but got: $props"
            .assertionError
    ).map(_.flatten)

    for
      edges <- builtEdges
      ends = edges.flatMap(e => List(e.source, e.target)).groupBy(_.hnId).toList
      _ <- ends.traverse((hnId, es) =>
        es.map(_.value).assertUniform(
          s"Seems bug: all SampleEdge ends which belong to the same hidden node ($hnId) must have " +
            s"the same index value, got ends = $es, for sampleId = $sampleId"
        )
      )
    yield edges

  def fromEdge[F[_]: MonadThrow](sourceHnId: HnId, targetHnId: HnId, edge: Relationship): F[List[SampleEdge]] =
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
      _ <- samples.map(_._1).assertDistinct("Sample edge can't be included to the same map edge multiple times")
    yield samples.map((sId, vals) =>
      SampleEdge(
        source = End(sourceHnId, vals._1),
        target = End(targetHnId, vals._2),
        edgeType = edgeType,
        sampleId = sId
      )
    )

  def fromNew[F[_]: MonadThrow](sampleId: SampleId, edge: New, indexies: Map[HnId, HnIndex]): F[SampleEdge] =
    def getIndex(hnId: HnId): F[HnIndex] = indexies.get(hnId) match
      case Some(id) => id.pure
      case _        => s"Missing HnIndex for $hnId in $indexies".assertionError

    for
      sourceValue <- getIndex(edge.source)
      targetValue <- getIndex(edge.target)
    yield SampleEdge(
      source = End(edge.source, sourceValue),
      target = End(edge.target, targetValue),
      edgeType = edge.edgeType,
      sampleId = sampleId
    )
