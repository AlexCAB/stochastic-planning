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
  private[edges] def indexies: Map[SampleId, Indexies]
  private[edges] def name: String

  private[edges] lazy val srcHnIndex: Set[HnIndex] = indexies.values.map(_.src).toSet
  private[edges] lazy val trgHnIndex: Set[HnIndex] = indexies.values.map(_.trg).toSet

  private[edges] def joinIndexies[F[_]: MonadThrow](b: Map[SampleId, Indexies]): F[Map[SampleId, Indexies]] =
    val aInd = indexies.values.toSet
    val bInd = b.values.toSet

    for
      _ <- indexies.keySet.assertNoSameElems(b.keySet, s"Map edge can't have duplicate $name sample")
      _ <- aInd.map(_.src).assertNoSameElems(bInd.map(_.src), s"Map edge can't have duplicate $name source indexes")
      _ <- aInd.map(_.trg).assertNoSameElems(bInd.map(_.trg), s"Map edge can't have duplicate $name target indexes")
    yield indexies ++ b

  private[edges] def addToMap[F[_]: MonadThrow](
      sId: SampleId,
      srcInd: HnIndex,
      trgInd: HnIndex
  ): F[Map[SampleId, Indexies]] =
    for
      _ <- indexies.keySet.assertNotContain(sId, s"Map edge can't have duplicate $name sample")
      _ <- indexies.values.map(_.src).assertNotContain(srcInd, s"Map edge can't have duplicate $name source index")
      _ <- indexies.values.map(_.trg).toSet.assertNotContain(trgInd, s"Map edge can't have dup $name target index")
    yield indexies + (sId -> Indexies(srcInd, trgInd))

  lazy val size: Int = indexies.size
  
  lazy val repr: String =
    s""""DcgEdgeSamples($name, indexies:
       |${indexies.map((sId, ix) => s"    ${sId.vStr} | ${ix.src.vStr} -> ${ix.trg.vStr}").mkString("\n")}
       |)""".stripMargin

  lazy val reprShort: String = 
    if indexies.nonEmpty then (if isInstanceOf[DcgEdgeSamples.Links] then "L" else "T") else "_"

  override lazy val toString: String = s"DcgEdgeSamples($name, indexies size = ${indexies.size})"

object DcgEdgeSamples:
  final case class Indexies(src: HnIndex, trg: HnIndex)

  final case class Links(indexies: Map[SampleId, Indexies]) extends DcgEdgeSamples:
    private[edges] def name: String = "links"

    def join[F[_]: MonadThrow](other: Links): F[Links] = joinIndexies(other.indexies).map(ixs => Links(ixs))

    def add[F[_]: MonadThrow](sId: SampleId, srcInd: HnIndex, trgInd: HnIndex): F[Links] =
      addToMap(sId, srcInd, trgInd).map(Links.apply)

  object Links:
    val empty: Links = Links(Map.empty)

  final case class Thens(indexies: Map[SampleId, Indexies]) extends DcgEdgeSamples:
    private[edges] def name: String = "thens"

    def join[F[_]: MonadThrow](other: Thens): F[Thens] = joinIndexies(other.indexies).map(ixs => Thens(ixs))

    def add[F[_]: MonadThrow](sId: SampleId, srcInd: HnIndex, trgInd: HnIndex): F[Thens] =
      addToMap(sId, srcInd, trgInd).map(Thens.apply)

  object Thens:
    val empty: Thens = Thens(Map.empty)
