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
import cats.syntax.all.*
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.{Description, Name}
import planning.engine.common.properties.*
import neotypes.model.types.Node
import planning.engine.common.values.db.Neo4j.SAMPLE_LABEL
import planning.engine.common.errors.assertionError

final case class SampleData(
    id: SampleId,
    probabilityCount: Long,
    utility: Double,
    name: Option[Name],
    description: Option[Description]
):
  override def toString: String =
    s"SampleData(id = $id, probabilityCount = $probabilityCount, utility = $utility, name = $name, description = $description)"

object SampleData:
  def fromNode[F[_]: MonadThrow](node: Node): F[SampleData] = node match
    case n if n.is(SAMPLE_LABEL) =>
      for
        id <- n.getValue[F, Long](PROP.SAMPLE_ID).map(SampleId.apply)
        probabilityCount <- n.getValue[F, Long](PROP.PROBABILITY_COUNT)
        utility <- n.getValue[F, Double](PROP.UTILITY)
        name <- n.getOptional[F, String](PROP.NAME).map(_.map(Name.apply))
        description <- n.getOptional[F, String](PROP.DESCRIPTION).map(_.map(Description.apply))
      yield SampleData(id, probabilityCount, utility, name, description)
    case _ => s"Node is not sample data: $node".assertionError
