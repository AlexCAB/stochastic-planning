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
import planning.engine.common.values.node.SnId
import planning.engine.planner.map.dcg.nodes.DcgNode

final case class DagNode[F[_]: MonadThrow, N <: DcgNode[F]](
    id: SnId,
    dcgNode: N
):
  override def toString: String = s"StateNode(id = ${id.value}, dcgNode.id = ${dcgNode.id.value})"
