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
| created: 2025-12-01 |||||||||||*/

package planning.engine.planner.map.dcg.nodes

import cats.MonadThrow
//import cats.effect.kernel.Concurrent
//import cats.effect.std.AtomicCell
//import cats.syntax.all.*
import planning.engine.common.values.node.HnId
//import MapNode.{Sources, Targets}
//import planning.engine.planner.map.dcg.edges.{LinkMapEdge, ThenMapEdge}

abstract class CachedNode[F[_]: MonadThrow](
    val id: HnId,
//    targets: Targets[F], // Targets of the links coming from previous (parent) nodes
//    sources: Sources[F]  // Sources of the links pointed to next (child) nodes
)

//object MapNode:
//  case class Targets[F[_]: MonadThrow](
//      linkEdges: AtomicCell[F, Map[HnId, LinkMapEdge[F]]],
//      thenEdges: AtomicCell[F, Map[HnId, ThenMapEdge[F]]]
//  )
//
//  object Targets:
//    def empty[F[_]: Concurrent]: F[Targets[F]] =
//      for
//        linkEdges <- AtomicCell[F].of(Map.empty[HnId, LinkMapEdge[F]])
//        thenEdges <- AtomicCell[F].of(Map.empty[HnId, ThenMapEdge[F]])
//      yield Targets(linkEdges, thenEdges)
//
//  case class Sources[F[_]: MonadThrow](
//      linkEdges: AtomicCell[F, Map[HnId, LinkMapEdge[F]]],
//      thenEdges: AtomicCell[F, Map[HnId, ThenMapEdge[F]]]
//  )
//
//  object Sources:
//    def empty[F[_]: Concurrent]: F[Sources[F]] =
//      for
//        linkEdges <- AtomicCell[F].of(Map.empty[HnId, LinkMapEdge[F]])
//        thenEdges <- AtomicCell[F].of(Map.empty[HnId, ThenMapEdge[F]])
//      yield Sources(linkEdges, thenEdges)
