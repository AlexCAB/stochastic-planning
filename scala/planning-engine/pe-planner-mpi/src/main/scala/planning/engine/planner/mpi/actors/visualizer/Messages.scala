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
| created: 18.06.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.visualizer

import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.common.repr.Representable

private[visualizer] trait Messages:
  sealed trait Message extends Representable

  object Structure:
    object Nodes:
      // Sent from ManagerActor after new nodes were added to the map network.
      final case class Added(ids: Map[MnId, Option[HnName]]) extends Message

    object Edges:
      // Sent from ManagerActor after edges were upserted in the map network.
      final case class Added(keys: Set[MeKey]) extends Message
