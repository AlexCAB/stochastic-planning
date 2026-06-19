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

package planning.engine.planner.mpi.adaptor

import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnName, MnId}

private[adaptor] trait Messages:
  sealed trait Message

  final case class NodeAdded(id: MnId, name: Option[HnName]) extends Message

  final case class EdgeAdded(key: MeKey) extends Message
  final case class EdgeError(key: MeKey, msg: String) extends Exception(msg) with Message
