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

private[adaptor] trait Messages:
  sealed trait Message

  final case class NodesAdded(ids: Map[MnId, Option[HnName]]) extends Message
  final case class NodesError(err: Throwable, ids: Set[MnId]) extends Exception(err) with Message

  final case class EdgeAdded(key: MeKey) extends Message
  final case class EdgeError(err: Throwable, key: MeKey) extends Exception(err) with Message
