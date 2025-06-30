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

package planning.engine.map.samples.sample.observed

import planning.engine.common.validation.Validation
import planning.engine.common.values.node.HnId
import planning.engine.common.values.text.{Description, Name}

final case class ObservedSample(
    probabilityCount: Long,
    utility: Double,
    name: Option[Name],
    description: Option[Description],
    edges: List[ObservedEdge]
) extends Validation:
  lazy val hnIds: List[HnId] = edges.flatMap(e => List(e.source, e.target)).distinct
  lazy val validationName: String = s"ObservedSample(name=$name, probabilityCount=$probabilityCount, utility=$utility)"

  lazy val validationErrors: List[Throwable] = validations(
    (probabilityCount > 0) -> "Probability count must be greater than 0",
    name.forall(_.value.nonEmpty) -> "Name must not be empty if defined",
    description.forall(_.value.nonEmpty) -> "Description must not be empty if defined",
    edges.nonEmpty -> "At least one edge must be provided",
    (edges.distinct == edges) -> "Edges must be unique by source and target"
  )

  override def toString: String = s"ObservedSample(" +
    s"probabilityCount=$probabilityCount, " +
    s"utility=$utility, " +
    s"name=$name, " +
    s"description=$description, " +
    s"hnIds=[${hnIds.map(_.value).mkString(", ")}], " +
    s"edges=[${edges.map(_.toString).mkString(", ")}])"
