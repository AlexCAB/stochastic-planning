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
import planning.engine.common.values.node.HnId
import planning.engine.common.values.sample.SampleId
import planning.engine.map.hidden.edge.HiddenEdge
import planning.engine.map.samples.sample.SampleEdge
import planning.engine.planner.map.dcg.edges.DcgEdgeData.EndIds
import planning.engine.common.errors.*
import planning.engine.planner.map.dcg.edges.DcgEdgeSamples.{Indexies, Links, Thens}

final case class DcgEdgeData(
    ends: EndIds,
    links: Links,
    thens: Thens
):
  lazy val hnIds: Set[HnId] = Set(ends.src, ends.trg)
  
  lazy val linksIds: Set[SampleId] = links.indexies.keySet
  lazy val thensIds: Set[SampleId] = thens.indexies.keySet
  lazy val sampleIds: Set[SampleId] = linksIds ++ thensIds

  lazy val isLink: Boolean = linksIds.nonEmpty
  lazy val isThen: Boolean = thensIds.nonEmpty

  def join[F[_]: MonadThrow](other: DcgEdgeData): F[DcgEdgeData] =
    for
      _ <- (ends, other.ends).assertEqual(s"Cannot join edges with different ends: $ends and ${other.ends}")
      newLinks <- links.join(other.links)
      newThens <- thens.join(other.thens)
    yield DcgEdgeData(ends, newLinks, newThens)

object DcgEdgeData:
  final case class EndIds(src: HnId, trg: HnId):
    lazy val swap: EndIds = EndIds(trg, src)

  private[edges] def makeDcgEdgeData(ends: EndIds, edgeType: EdgeType, indexies: IndexMap): DcgEdgeData = edgeType match
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
      _ <- (edgeKeys, keyValues).assertSameElems(s"Edge keys from SampleEdges do not match the provided key: $edgeKeys")
      _ <- edges.map(_.sampleId).assertDistinct("Duplicate SampleIds in SampleEdges detected")
      _ <- edges.map(_.source.value).assertDistinct("Duplicate Source value in SampleEdges detected")
      _ <- edges.map(_.target.value).assertDistinct("Duplicate Target value in SampleEdges detected")
      samples = edges.map(e => e.sampleId -> Indexies(e.source.value, e.target.value)).toMap
    yield makeDcgEdgeData(ends, edgeType, samples)
