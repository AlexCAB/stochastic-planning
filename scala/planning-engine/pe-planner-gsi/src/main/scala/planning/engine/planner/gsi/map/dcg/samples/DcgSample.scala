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

package planning.engine.planner.gsi.map.dcg.samples

import cats.syntax.all.*
import cats.syntax.ext.*
import planning.engine.common.errors.*
import planning.engine.common.graph.GraphStructure
import planning.engine.common.graph.edges.{IndexMap, MeKey}
import planning.engine.common.values.node.MnId.{Abs, Con}
import planning.engine.common.values.sample.SampleId
import planning.engine.map.samples.sample.{Sample, SampleData}
import planning.engine.planner.gsi.map.repr.DcgSampleRepr

final case class DcgSample[F[_]: MT](
    data: SampleData,
    structure: GraphStructure,
) extends DcgSampleRepr[F]:
  override lazy val toString: String =
    s"DcgSample(${data.id.vStr}${data.name.repr}, edges sizes: ${structure.keys.size})"

object DcgSample:
  final case class Add[F[_]: MT](
      sample: DcgSample[F],
      indexMap: IndexMap, // Value indexies map should be provided from outside, from DB of from fast counts.
  ):
    lazy val idsByKey: Set[(MeKey, (SampleId, IndexMap))] = sample
      .structure.keys
      .map(k => k -> (sample.data.id, indexMap))

  def apply[F[_]: MT](
      id: SampleId,
      sample: Sample.New,
      conMnId: Set[Con],
      absMnId: Set[Abs],
  ): F[DcgSample[F]] =
    for
      keys <- sample.edges.toList.traverse(e => MeKey(e.edgeType, e.source, e.target, conMnId, absMnId))
      _ <- keys.assertDistinct("Sample edges must be distinct")
      data = sample.toSampleData(id)
      structure = GraphStructure(keys.toSet)
    yield new DcgSample(data, structure)

  def apply[F[_]: MT](data: SampleData, structure: GraphStructure): F[DcgSample[F]] =
    for
      _ <- data.probabilityCount.assertPositive("Sample probability count must be positive")
      _ <- structure.isConnected.assertTrue("DcgSample edges must form a connected graph")
    yield new DcgSample(data, structure)
