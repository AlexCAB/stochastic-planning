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
import planning.engine.common.values.node.SnId
import planning.engine.planner.map.dcg.edges.DcgEdge

final case class DagEdge[F[_]: MonadThrow](
    key: DagEdge.Key,
    dcgEdge: DcgEdge[F]
):
  lazy val isLink: Boolean = dcgEdge.key.isLink
  lazy val isThen: Boolean = dcgEdge.key.isThen
  
  override lazy val toString: String = s"${key.src.repr} ${dcgEdge.key.reprArrow} ${key.trg.repr}"

object DagEdge:
  final case class Key(src: SnId, trg: SnId):
    override lazy val toString: String = s"${src.repr} --> ${trg.repr}"
