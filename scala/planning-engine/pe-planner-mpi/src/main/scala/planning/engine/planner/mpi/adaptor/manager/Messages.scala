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
| created: 19.06.2026 |||||||||||*/

package planning.engine.planner.mpi.adaptor.manager

import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.common.repr.Representable

private[adaptor] trait Messages:
  sealed trait Message extends Representable

  final case class NodesAdded(ids: Map[MnId, Option[HnName]]) extends Message
  final case class NodesUpserted(ids: Map[MnId, Option[HnName]]) extends Message
  
  final case class EdgesUpserted(keys: Set[MeKey]) extends Message
