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
| created: 2025-04-07 |||||||||||*/

package planning.engine.core.map.hidden.state

import cats.MonadThrow
import planning.engine.core.map.hidden.node.HiddenNode

sealed trait NodeState[F[_]: MonadThrow]

case class InitNodeState[F[_]: MonadThrow]() extends NodeState[F]

case class RootNodeSate[F[_]: MonadThrow](children: Vector[SampleState[F]]) extends NodeState

case class LeafNodeSate[F[_]: MonadThrow](parents: Vector[HiddenNode[F]]) extends NodeState

case class IntermediateNodeState[F[_]: MonadThrow](
    parents: Vector[HiddenNode[F]],
    children: Vector[SampleState[F]]
) extends NodeState

case class RemovedNodeState[F[_]: MonadThrow]() extends NodeState[F]
