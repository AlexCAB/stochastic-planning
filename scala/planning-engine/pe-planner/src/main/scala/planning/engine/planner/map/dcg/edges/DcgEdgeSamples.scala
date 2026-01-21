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
import planning.engine.common.values.node.HnIndex
import planning.engine.common.errors.*
import planning.engine.planner.map.dcg.edges.DcgEdgeSamples.Indexies

sealed trait DcgEdgeSamples:
  private[edges] def joinIndexies[F[_]: MonadThrow](
      a: Map[SampleId, Indexies],
      b: Map[SampleId, Indexies],
      name: String
  ): F[Map[SampleId, Indexies]] =
    val aInd = a.values.toSet
    val bInd = b.values.toSet

    for
      _ <- (a.keySet, b.keySet).assertNoSameElems(s"Map edge can't have duplicate $name sample")
      _ <- (aInd.map(_.src), bInd.map(_.src)).assertNoSameElems(s"Map edge can't have duplicate $name source indexes")
      _ <- (aInd.map(_.trg), bInd.map(_.trg)).assertNoSameElems(s"Map edge can't have duplicate $name target indexes")
    yield a ++ b

object DcgEdgeSamples:
  final case class Indexies(src: HnIndex, trg: HnIndex)

  final case class Links(indexies: Map[SampleId, Indexies]) extends DcgEdgeSamples:
    def join[F[_]: MonadThrow](other: Links): F[Links] =
      joinIndexies(this.indexies, other.indexies, "links").map(ixs => Links(ixs))

  object Links:
    val empty: Links = Links(Map.empty)

  final case class Thens(indexies: Map[SampleId, Indexies]) extends DcgEdgeSamples:
    def join[F[_]: MonadThrow](other: Thens): F[Thens] =
      joinIndexies(this.indexies, other.indexies, "thens").map(ixs => Thens(ixs))

  object Thens:
    val empty: Thens = Thens(Map.empty)
