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
import planning.engine.common.values.edge.{EdgeKey, IndexMap, Indexies}
import planning.engine.map.hidden.edge.HiddenEdge.SampleIndexies

final case class DcgSamples[F[_]: MonadThrow](indexies: Map[SampleId, Indexies]):
  lazy val size: Int = indexies.size
  lazy val isEmpty: Boolean = indexies.isEmpty
  lazy val sampleIds: Set[SampleId] = indexies.keySet

  lazy val srcHnIndex: Set[HnIndex] = indexies.values.map(_.src).toSet
  lazy val trgHnIndex: Set[HnIndex] = indexies.values.map(_.trg).toSet

  def join(other: DcgSamples[F]): F[DcgSamples[F]] =
    for
      _ <- indexies.keySet.assertContainsNoneOf(other.indexies.keySet, "Map edge can't have duplicate sample")
      _ <- srcHnIndex.assertContainsNoneOf(other.srcHnIndex, "Map edge can't have duplicate source indexes")
      _ <- trgHnIndex.assertContainsNoneOf(other.trgHnIndex, "Map edge can't have duplicate target indexes")
    yield this.copy(indexies = indexies ++ other.indexies)

  override lazy val toString: String = indexies
    .toList.sortBy(_._1.value)
    .map((sId, ix) => s"${sId.vStr} | ${ix.src.vStr}->${ix.trg.vStr}")
    .mkString(", ")


object DcgSamples:
  def empty[F[_]: MonadThrow]: DcgSamples[F] = new DcgSamples(Map.empty)

  def apply[F[_] : MonadThrow](sId: SampleId, srcInd: HnIndex, trgInd: HnIndex): DcgSamples[F] =
    new DcgSamples(Map(sId -> Indexies(srcInd, trgInd)))
  
  def apply[F[_] : MonadThrow](indexies: Map[SampleId, Indexies]): F[DcgSamples[F]] =
    for
      _ <- indexies.map((_, i) => i.src).assertDistinct("Map edge can't have duplicate source index")
      _ <- indexies.map((_, i) => i.trg).assertDistinct("Map edge can't have duplicate target index")
    yield new DcgSamples(indexies)  

  def fromSamples[F[_]: MonadThrow](samples: Iterable[SampleIndexies]): F[DcgSamples[F]] =
    for
      _ <- samples.map(_.sampleId).assertDistinct("Map edge can't have duplicate sample")
      indexies = samples.map(s => s.sampleId -> Indexies(s.sourceIndex, s.targetIndex)).toMap
      dcgSamples <- DcgSamples(indexies)
    yield dcgSamples

  def fromIndexMap[F[_]: MonadThrow](key: EdgeKey, indexMap: Map[SampleId, IndexMap]): F[DcgSamples[F]] =
    for
      indexies <- indexMap.toList.traverse((sId, ind) => ind.get(key.src, key.trg).map(i => sId -> i)).map(_.toMap)
      dcgSamples <- DcgSamples(indexies)
    yield dcgSamples 
