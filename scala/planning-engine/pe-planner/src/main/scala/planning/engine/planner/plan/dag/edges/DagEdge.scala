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
| created: 2026-01-04 |||||||||||*/

package planning.engine.planner.plan.dag.edges

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.errors.assertEqual
import planning.engine.common.graph.edges.PeKey
import planning.engine.planner.map.dcg.edges.DcgEdge

final case class DagEdge[F[_]: MonadThrow](
    key: PeKey,
    dcgEdge: DcgEdge[F]
):
  lazy val isLink: Boolean = dcgEdge.key.isLink
  lazy val isThen: Boolean = dcgEdge.key.isThen

  override lazy val toString: String = s"${key.src.repr}${dcgEdge.key.reprArrow}${key.trg.repr}"

object DagEdge:
  def apply[F[_]: MonadThrow](key: PeKey, dcgEdge: DcgEdge[F]): F[DagEdge[F]] =
    for
      _ <- key.src.mnId.assertEqual(dcgEdge.key.src, "Source MnIds must match between PeKey and DcgEdge")
      _ <- key.trg.mnId.assertEqual(dcgEdge.key.trg, "Target MnIds must match between PeKey and DcgEdge")
    yield new DagEdge(key, dcgEdge)
