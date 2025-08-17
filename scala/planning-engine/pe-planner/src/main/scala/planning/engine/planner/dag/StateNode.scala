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

package planning.engine.planner.dag

import cats.MonadThrow
import cats.effect.Concurrent
import cats.effect.std.AtomicCell
import planning.engine.common.values.node.HnId
import planning.engine.common.values.text.Name
import planning.engine.planner.dag.StateNode.{Parameters, Structure}
import cats.syntax.all.*

abstract class StateNode[F[_]: MonadThrow](
    structure: AtomicCell[F, Structure[F]],
    parameters: AtomicCell[F, Parameters]
):
  def hnId: HnId
  def name: Option[Name]

object StateNode:
  enum Kind:
    case Past, Present, Plan

  final case class Structure[F[_]: MonadThrow](
      linkParents: Set[StateNode[F]],
      linkChildren: Set[StateNode[F]],
      thenParents: Set[StateNode[F]],
      thenChildren: Set[StateNode[F]]
  )

  object Structure:
    def init[F[_]: MonadThrow](
        linkParents: Set[StateNode[F]],
        thenParents: Set[StateNode[F]]
    ): Structure[F] = Structure(
      linkParents = linkParents,
      linkChildren = Set.empty,
      thenParents = thenParents,
      thenChildren = Set.empty
    )

  final case class Parameters(
      kind: Kind,
      probability: Double,
      utility: Double
  )

  object Parameters:
    lazy val init: Parameters = Parameters(
      kind = Kind.Present,
      probability = 0.0,
      utility = 0.0
    )

  def initState[F[_]: Concurrent](
      linkParents: Set[StateNode[F]],
      thenParents: Set[StateNode[F]]
  ): F[(AtomicCell[F, Structure[F]], AtomicCell[F, Parameters])] =
    for
      structure <- AtomicCell[F].of(Structure.init(linkParents, thenParents))
      parameters <- AtomicCell[F].of(Parameters.init)
    yield (structure, parameters)
