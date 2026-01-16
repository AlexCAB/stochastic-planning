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
| created: 2026-01-15 |||||||||||*/

package planning.engine.planner.map.dcg.edges

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.values.sample.SampleId
import planning.engine.common.errors.assertionError
import planning.engine.common.values.node.HnIndex
import planning.engine.planner.map.dcg.edges.DcgEdgeSamples.Indexies

type IndexMap = Map[SampleId, Indexies]

sealed trait DcgEdgeSamples:
  private[edges] def joinIndexies[F[_]: MonadThrow](a: IndexMap, b: IndexMap, name: String): F[IndexMap] =
    val int = a.keySet.intersect(b.keySet)
    if int.nonEmpty
    then s"Map edge can't have duplicate $name samples: $int".assertionError
    else (a ++ b).pure

object DcgEdgeSamples:
  final case class Indexies(src: HnIndex, trg: HnIndex)

  final case class Links(indexies: IndexMap) extends DcgEdgeSamples:
    def join[F[_]: MonadThrow](other: Links): F[Links] =
      joinIndexies(this.indexies, other.indexies, "links").map(ixs => Links(ixs))

  object Links:
    val empty: Links = Links(Map.empty)

  final case class Thens(indexies: IndexMap) extends DcgEdgeSamples:
    def join[F[_]: MonadThrow](other: Thens): F[Thens] =
      joinIndexies(this.indexies, other.indexies, "thens").map(ixs => Thens(ixs))

  object Thens:
    val empty: Thens = Thens(Map.empty)
