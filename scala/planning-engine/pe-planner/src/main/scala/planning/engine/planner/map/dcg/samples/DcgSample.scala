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
| created: 2026-01-26 |||||||||||*/

package planning.engine.planner.map.dcg.samples

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.graph.GraphStructure
import planning.engine.map.samples.sample.{Sample, SampleData}
import planning.engine.common.values.edge.{EdgeKey, IndexMap}
import planning.engine.common.values.node.MnId.{Abs, Con}
import planning.engine.common.values.sample.SampleId
import planning.engine.common.errors.*
import planning.engine.planner.map.dcg.repr.DcgSampleRepr

final case class DcgSample[F[_]: MonadThrow](
    data: SampleData,
    structure: GraphStructure[F]
) extends DcgSampleRepr[F]:
  override lazy val toString: String =
    s"DcgSample(${data.id.vStr}${data.name.repr}, edges sizes: ${structure.keys.size})"

object DcgSample:
  final case class Add[F[_]: MonadThrow](
      sample: DcgSample[F],
      indexMap: IndexMap // Value indexies map should be provided from outside, from DB of from fast counts.
  ):
    lazy val idsByKey: Set[(EdgeKey, (SampleId, IndexMap))] = sample
      .structure.keys
      .map(k => k -> (sample.data.id, indexMap))

  def apply[F[_]: MonadThrow](
      id: SampleId,
      sample: Sample.New,
      conMnId: Set[Con],
      absMnId: Set[Abs]
  ): F[DcgSample[F]] =
    for
      keys <- sample.edges.toList.traverse(e => EdgeKey(e.edgeType, e.source, e.target, conMnId, absMnId))
      _ <- keys.assertDistinct("Sample edges must be distinct")
      data = sample.toSampleData(id)
      structure = GraphStructure(keys.toSet)
    yield new DcgSample(data, structure)

  def apply[F[_]: MonadThrow](data: SampleData, structure: GraphStructure[F]): F[DcgSample[F]] =
    for
      _ <- data.probabilityCount.assertPositive("Sample probability count must be positive")
      _ <- structure.isConnected.assertTrue("DcgSample edges must form a connected graph")
    yield new DcgSample(data, structure)
