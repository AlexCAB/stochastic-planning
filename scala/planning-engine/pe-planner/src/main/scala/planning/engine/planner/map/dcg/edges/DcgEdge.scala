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
| created: 2025-12-01 |||||||||||*/

package planning.engine.planner.map.dcg.edges

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.{HnId, HnIndex, MnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.map.hidden.edge.HiddenEdge
import planning.engine.map.samples.sample.SampleEdge
import planning.engine.common.values.edges.{Edge, Ends}
import planning.engine.common.errors.*
import planning.engine.planner.map.dcg.edges.DcgSamples.{Indexies, Links, Thens}
import planning.engine.planner.map.dcg.repr.DcgEdgeDataRepr

sealed trait DcgEdge[F[_]: MonadThrow]:
  def ends: Edge
  def samples: DcgSamples
  


final case class DcgEdgeData[F[_]: MonadThrow](
    ends: Ends,
    links: Links,
    thens: Thens
) extends DcgEdgeDataRepr:
  lazy val hnIds: Set[HnId] = Set(ends.src, ends.trg)

  lazy val srcHnIndex: Set[HnIndex] = links.srcHnIndex ++ thens.srcHnIndex
  lazy val trgHnIndex: Set[HnIndex] = links.trgHnIndex ++ thens.trgHnIndex

  lazy val linksIds: Set[SampleId] = links.indexies.keySet
  lazy val thensIds: Set[SampleId] = thens.indexies.keySet
  lazy val sampleIds: Set[SampleId] = linksIds ++ thensIds

  lazy val isLink: Boolean = linksIds.nonEmpty
  lazy val isThen: Boolean = thensIds.nonEmpty

  def toEdge(conIds: Set[HnId], absIds: Set[HnId]): F[Set[Edge]] =
    for
      links <- if isLink then ends.asLink(n => MnId.fromHnId(n, conIds, absIds)).map(Set(_)) else Set().pure
      thens <- if isThen then ends.asThen(n => MnId.fromHnId(n, conIds, absIds)).map(Set(_)) else Set().pure
    yield links ++ thens

  def addLink(sampleId: SampleId, srcInd: HnIndex, trgInd: HnIndex): F[DcgEdgeData[F]] =
    links.add(sampleId, srcInd, trgInd).map(newLinks => this.copy(links = newLinks))

  def addThen(sampleId: SampleId, srcInd: HnIndex, trgInd: HnIndex): F[DcgEdgeData[F]] =
    thens.add(sampleId, srcInd, trgInd).map(newThens => this.copy(thens = newThens))

  def addSample(et: EdgeType, sId: SampleId, indexies: Map[HnId, HnIndex]): F[DcgEdgeData[F]] =
    (et, indexies.get(ends.src), indexies.get(ends.trg)) match
      case (EdgeType.LINK, Some(srcInd), Some(trgInd)) => addLink(sId, srcInd, trgInd)
      case (EdgeType.THEN, Some(srcInd), Some(trgInd)) => addThen(sId, srcInd, trgInd)
      case (_, srcInd, trgInd) => s"Source ($srcInd) or Target ($trgInd) HnId not found in indexies map".assertionError

  def join[F[_]: MonadThrow](other: DcgEdgeData[F]): F[DcgEdgeData[F]] =
    for
      _ <- ends.assertEqual(other.ends, s"Cannot join edges with different ends: $ends and ${other.ends}")
      newLinks <- links.join(other.links)
      newThens <- thens.join(other.thens)
    yield DcgEdgeData(ends, newLinks, newThens)

  override lazy val toString: String =
    s"DcgEdgeData(${ends.src.vStr} -> ${ends.trg.vStr}, links size = ${links.size}, thens size = ${thens.size})"

object DcgEdgeData:
  private[edges] def makeDcgEdgeData[F[_]: MonadThrow](
      ends: Ends,
      edgeType: EdgeType,
      indexies: Map[SampleId, Indexies]
  ): DcgEdgeData[F] = edgeType match
    case EdgeType.LINK => DcgEdgeData(ends, Links(indexies), Thens.empty)
    case EdgeType.THEN => DcgEdgeData(ends, Links.empty, Thens(indexies))

  def apply[F[_]: MonadThrow](edge: HiddenEdge): DcgEdgeData[F] = makeDcgEdgeData(
    Ends(edge.sourceId, edge.targetId),
    edge.edgeType,
    edge.samples.map(s => s.sampleId -> Indexies(s.sourceIndex, s.targetIndex)).toMap
  )

  def apply[F[_]: MonadThrow](edgeType: EdgeType, ends: Ends, edges: List[SampleEdge]): F[DcgEdgeData[F]] =
    for
      _ <- edges.assertNonEmpty("SampleEdges list is empty")
      edgeKeys = edges.map(e => (e.edgeType, e.source.hnId, e.target.hnId)).toSet
      keyValues = Set((edgeType, ends.src, ends.trg))
      _ <- edgeKeys.assertSameElems(keyValues, s"Edge keys from SampleEdges do not match the provided key: $edgeKeys")
      _ <- edges.map(_.sampleId).assertDistinct("Duplicate SampleIds in SampleEdges detected")
      _ <- edges.map(_.source.value).assertDistinct("Duplicate Source value in SampleEdges detected")
      _ <- edges.map(_.target.value).assertDistinct("Duplicate Target value in SampleEdges detected")
      samples = edges.map(e => e.sampleId -> Indexies(e.source.value, e.target.value)).toMap
    yield makeDcgEdgeData(ends, edgeType, samples)
