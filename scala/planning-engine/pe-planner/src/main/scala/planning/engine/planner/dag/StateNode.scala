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
import planning.engine.common.values.node.{HnId, IoIndex}
import planning.engine.common.values.text.Name
import planning.engine.planner.dag.StateNode.{Kind, Parameters, Structure}
import cats.syntax.all.*
import planning.engine.common.values.io.Time
import planning.engine.common.errors.assertionError

abstract class StateNode[F[_]: MonadThrow](
    structure: AtomicCell[F, Structure[F]],
    parameters: AtomicCell[F, Parameters]
):
  def hnId: HnId
  def name: Option[Name]

  def isBelongsToIo(ioNodeName: Name, valueIndex: IoIndex): Boolean

  // Returns the updated list of values (with removed fount entries)
  // and this node if it was moved to Present kind.
  def markAsPresentIfInValues(values: Map[Name, IoIndex]): F[(Map[Name, IoIndex], Option[StateNode[F]])] =
    parameters.evalModify(params =>
      values.find((n, i) => isBelongsToIo(n, i)) match

        // If this node is mentioned in IO values, and it is in Plan, we change its kind to Present,
        // which means that it is now part of the current context (mowing node from plan to context)
        case Some((ioName, index)) if params.kind == Kind.Plan =>
          (params.copy(kind = Kind.Present), (values.removed(ioName), this.some)).pure

        case Some((ioName, index)) =>
          (s"Incorrect state of StateNode, expected params.kind == Plan, but found ${params.kind}, " +
            s"for ioName = $ioName and index = $index " +
            s"likely synchronisation bug, this node was moved in to context (i.e. kind changed to " +
            s"past or present) twice").assertionError

        case None =>
          // If the node is not in the values, we do not change its parameters
          (params, (values, None)).pure
    )

  def markThenChildrenAsPresentIfInValues(values: Map[Name, IoIndex]): F[(Map[Name, IoIndex], Set[StateNode[F]])] =
    structure.get
      .flatMap(_.thenChildren.foldLeft((values, Set[StateNode[F]]()).pure)((acc, child) =>
        for
          (vs, found) <- acc
          (newVs, nodeOpt) <- child.markAsPresentIfInValues(vs)
        yield (newVs, found ++ nodeOpt.toSet)
      ))

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
      observationTime: Option[Time], // Wil be set if the node is observed by receiving Observation(...) signal
      probability: Double,
      utility: Double
  )

  object Parameters:
    lazy val init: Parameters = Parameters(
      kind = Kind.Present,
      observationTime = None,
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
