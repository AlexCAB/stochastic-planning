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
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.common.values.sample.SampleId
import planning.engine.map.hidden.edge.HiddenEdge
import planning.engine.map.samples.sample.SampleEdge
import planning.engine.common.values.edges.EndIds
import planning.engine.common.errors.*
import planning.engine.planner.map.dcg.edges.DcgEdgeSamples.{Indexies, Links, Thens}

final case class DcgEdgeData(
    ends: EndIds,
    links: Links,
    thens: Thens
):
  lazy val hnIds: Set[HnId] = Set(ends.src, ends.trg)

  lazy val srcHnIndex: Set[HnIndex] = links.srcHnIndex ++ thens.srcHnIndex
  lazy val trgHnIndex: Set[HnIndex] = links.trgHnIndex ++ thens.trgHnIndex

  lazy val linksIds: Set[SampleId] = links.indexies.keySet
  lazy val thensIds: Set[SampleId] = thens.indexies.keySet
  lazy val sampleIds: Set[SampleId] = linksIds ++ thensIds

  lazy val isLink: Boolean = linksIds.nonEmpty
  lazy val isThen: Boolean = thensIds.nonEmpty

  def addLink[F[_]: MonadThrow](sampleId: SampleId, srcInd: HnIndex, trgInd: HnIndex): F[DcgEdgeData] =
    links.add(sampleId, srcInd, trgInd).map(newLinks => this.copy(links = newLinks))

  def addThen[F[_]: MonadThrow](sampleId: SampleId, srcInd: HnIndex, trgInd: HnIndex): F[DcgEdgeData] =
    thens.add(sampleId, srcInd, trgInd).map(newThens => this.copy(thens = newThens))

  def addSample[F[_]: MonadThrow](et: EdgeType, sId: SampleId, indexies: Map[HnId, HnIndex]): F[DcgEdgeData] =
    (et, indexies.get(ends.src), indexies.get(ends.trg)) match
      case (EdgeType.LINK, Some(srcInd), Some(trgInd)) => addLink(sId, srcInd, trgInd)
      case (EdgeType.THEN, Some(srcInd), Some(trgInd)) => addThen(sId, srcInd, trgInd)
      case (_, srcInd, trgInd) => s"Source ($srcInd) or Target ($trgInd) HnId not found in indexies map".assertionError

  def join[F[_]: MonadThrow](other: DcgEdgeData): F[DcgEdgeData] =
    for
      _ <- ends.assertEqual(other.ends, s"Cannot join edges with different ends: $ends and ${other.ends}")
      newLinks <- links.join(other.links)
      newThens <- thens.join(other.thens)
    yield DcgEdgeData(ends, newLinks, newThens)
    
  lazy val repr: String =  s"(${ends.src.vStr}) -${links.reprShort}${thens.reprShort}-> (${ends.trg.vStr})"
  lazy val reprTarget: String =  s"| -${links.reprShort}${thens.reprShort}-> (${ends.trg.vStr}) "
  
  override lazy val toString: String = 
    s"DcgEdgeData(${ends.src.vStr} -> ${ends.trg.vStr}, links size = ${links.size}, thens size = ${thens.size})"
        
object DcgEdgeData:
  private[edges] def makeDcgEdgeData(
      ends: EndIds,
      edgeType: EdgeType,
      indexies: Map[SampleId, Indexies]
  ): DcgEdgeData = edgeType match
    case EdgeType.LINK => DcgEdgeData(ends, Links(indexies), Thens.empty)
    case EdgeType.THEN => DcgEdgeData(ends, Links.empty, Thens(indexies))

  def apply(edge: HiddenEdge): DcgEdgeData = makeDcgEdgeData(
    EndIds(edge.sourceId, edge.targetId),
    edge.edgeType,
    edge.samples.map(s => s.sampleId -> Indexies(s.sourceIndex, s.targetIndex)).toMap
  )

  def apply[F[_]: MonadThrow](edgeType: EdgeType, ends: EndIds, edges: List[SampleEdge]): F[DcgEdgeData] =
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
