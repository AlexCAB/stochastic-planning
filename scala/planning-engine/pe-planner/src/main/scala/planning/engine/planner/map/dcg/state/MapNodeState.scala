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
| created: 2025-12-02 |||||||||||*/

package planning.engine.planner.map.dcg.state

import cats.MonadThrow
import cats.effect.kernel.Concurrent
import cats.effect.std.AtomicCell
import cats.syntax.all.*
import planning.engine.common.values.node.HnId
import planning.engine.planner.map.dcg.edges.{LinkMapEdge, ThenMapEdge}

object MapNodeState:
  case class Patents[F[_]: MonadThrow](
      linkEdges: AtomicCell[F, Map[HnId, LinkMapEdge[F]]],
      thenEdges: AtomicCell[F, Map[HnId, ThenMapEdge[F]]]
  )

  case class Children[F[_]: MonadThrow](
      linkEdges: AtomicCell[F, Map[HnId, LinkMapEdge[F]]],
      thenEdges: AtomicCell[F, Map[HnId, ThenMapEdge[F]]]
  )

  def makeEdgesState[F[_]: Concurrent](
      linkPatents: Map[HnId, LinkMapEdge[F]],
      thenPatents: Map[HnId, ThenMapEdge[F]],
      linkChildren: Map[HnId, LinkMapEdge[F]],
      thenChildren: Map[HnId, ThenMapEdge[F]]
  ): F[(Patents[F], Children[F])] =
    for
      linkPatentsState <- AtomicCell[F].of(linkPatents)
      thenPatentsState <- AtomicCell[F].of(thenPatents)
      linkChildrenState <- AtomicCell[F].of(linkChildren)
      thenChildrenState <- AtomicCell[F].of(thenChildren)
    yield (Patents(linkPatentsState, thenPatentsState), Children(linkChildrenState, thenChildrenState))
