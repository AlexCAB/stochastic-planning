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
| created: 2025-05-11 |||||||||||*/

package planning.engine.map.samples.sample

import cats.MonadThrow
import neotypes.query.QueryArg.Param
import planning.engine.common.properties.*
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.common.values.sample.SampleId
import cats.syntax.all.*
import planning.engine.common.validation.Validation
import planning.engine.common.values.text.{Description, Name}
import planning.engine.common.errors.assertionError
import planning.engine.common.values.StringVal.toStr

final case class Sample(
    data: SampleData,
    edges: List[SampleEdge]
):
  override def toString: String =
    s"Sample(id = ${data.id}, name = ${data.name}, count=${data.probabilityCount}, utility=${data.utility}"

object Sample:
  final case class New(
      probabilityCount: Long,
      utility: Double,
      name: Option[Name],
      description: Option[Description],
      edges: Set[SampleEdge.New]
  ) extends Validation:
    lazy val hnIds: Set[HnId] = edges.flatMap(e => List(e.source, e.target))
    lazy val validationName: String =
      s"Sample(name=${name.toStr}, probabilityCount=$probabilityCount, utility=$utility)"

    lazy val validationErrors: List[Throwable] = validations(
      (probabilityCount > 0) -> "Probability count must be greater than 0",
      name.forall(_.value.nonEmpty) -> "Name must not be empty if defined",
      description.forall(_.value.nonEmpty) -> "Description must not be empty if defined",
      edges.nonEmpty -> "At least one edge must be provided"
    )

    def findHnIndexies[F[_]: MonadThrow](hnInsToHnIndex: Map[HnId, List[HnIndex]])
        : F[(Map[HnId, List[HnIndex]], Map[HnId, HnIndex])] = hnIds
      .foldRight((Map[HnId, List[HnIndex]](), Map[HnId, HnIndex]()).pure):
        case (hnId, acc) => hnInsToHnIndex.get(hnId) match
            case Some(inx :: hnIxs) => acc.map((ixs, ix) => (ixs + (hnId -> hnIxs), ix + (hnId -> inx)))
            case _ => s"Missing HnIndex for hnId = $hnId, in hnInsToHnIndex = $hnInsToHnIndex".assertionError

    def toQueryParams[F[_]: MonadThrow](sampleId: SampleId): F[Map[String, Param]] = paramsOf(
      PROP.SAMPLE_ID -> sampleId.toDbParam,
      PROP.PROBABILITY_COUNT -> probabilityCount.toDbParam,
      PROP.UTILITY -> utility.toDbParam,
      PROP.NAME -> name.map(_.toDbParam),
      PROP.DESCRIPTION -> description.map(_.toDbParam)
    )

    override def toString: String = s"Sample.New(" +
      s"probabilityCount=$probabilityCount, " +
      s"utility=$utility, " +
      s"name=${name.toStr}, " +
      s"description=${description.toStr}, " +
      s"hnIds=[${hnIds.map(_.value).mkString(", ")}], " +
      s"edges=[${edges.map(_.toString).mkString(", ")}])"

  final case class ListNew(list: Set[New]):

    lazy val allEdges: Set[SampleEdge.New] = list.flatMap(_.edges)
    lazy val allHnIds: Set[HnId] = allEdges.flatMap(e => List(e.source, e.target))

    lazy val numHnIndexPerHn: Map[HnId, Int] = allHnIds.map(id => id -> list.count(_.hnIds.contains(id))).toMap

  object ListNew:
    def of(samples: New*): ListNew = ListNew(samples.toSet)
