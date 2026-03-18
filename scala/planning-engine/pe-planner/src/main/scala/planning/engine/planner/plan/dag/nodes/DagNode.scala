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
| created: 2025-08-14 |||||||||||*/

package planning.engine.planner.plan.dag.nodes

import cats.MonadThrow
import planning.engine.common.values.node.PnId
import planning.engine.planner.map.dcg.nodes.DcgNode

final case class DagNode[F[_]: MonadThrow](
                                            id: PnId,
                                            dcgNode: DcgNode[F]
):
  override lazy val toString: String = s"${dcgNode.repr}_${id.time.repr}"
