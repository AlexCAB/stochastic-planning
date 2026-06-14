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
| created: 14.06.2026 |||||||||||*/

package planning.engine.planner.mpi.model.messages

import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnName, MnId}

object AdaptorMsg:
  sealed trait NodeResult

  final case class NodeAdded(id: MnId, name: Option[HnName]) extends NodeResult

  sealed trait EdgeResult:
    def key: MeKey

  final case class EdgeAdded(key: MeKey) extends EdgeResult

  final case class EdgeError(key: MeKey, msg: String) extends Exception(msg) with EdgeResult
