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

package planning.engine.planner.gsi.plan.dag.nodes

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.errors.assertEqual
import planning.engine.common.values.io.IoTime
import planning.engine.common.values.node.PnId
import planning.engine.planner.gsi.map.dcg.nodes.DcgNode

final case class DagNode[F[_]: MonadThrow](
    id: PnId,

    // Agent world time (step), it set for active nodes, i.e. for nodes in context.
    // And for nodes in plan it is empty (because current implementation of map do not store info about
    // time of event (only sequence of events), so it can't be predicted).
    time: Option[IoTime],

    // Corresponding DcgNode. One DcgNode can be linked to multiple DagNodes.
    dcgNode: DcgNode[F]
):
  import IoTime.*

  lazy val repr: String = dcgNode match
    case cn: DcgNode.Concrete[F] => s"[${time.repr}, ${id.reprCount}, ${cn.idRepr}, ${cn.ioValue.repr}]"
    case an: DcgNode.Abstract[F] => s"(${time.repr}, ${id.reprCount}, ${an.idRepr})"

  override lazy val toString: String = s"${dcgNode.repr}_${time.repr}"

object DagNode:
  def apply[F[_]: MonadThrow](id: PnId, time: Option[IoTime], dcgNode: DcgNode[F]): F[DagNode[F]] =
    for
        _ <- id.mnId.assertEqual(dcgNode.id, "MnIds must match between PnId and DcgNode")
    yield new DagNode(id, time, dcgNode)
