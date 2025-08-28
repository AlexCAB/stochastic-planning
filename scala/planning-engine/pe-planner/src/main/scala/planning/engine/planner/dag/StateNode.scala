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
import planning.engine.common.values.node.{HnId, IoIndex, SnId}
import planning.engine.common.values.text.Name
import planning.engine.planner.dag.StateNode.{Kind, Parameters, Structure}
import cats.syntax.all.*
import planning.engine.common.values.io.Time
import planning.engine.common.errors.assertionError

abstract class StateNode[F[_]: MonadThrow](
    structure: AtomicCell[F, Structure[F]],
    parameters: AtomicCell[F, Parameters]
):
  def id: SnId
  def hnId: HnId
  def name: Option[Name]

  def getStructure: F[Structure[F]] = structure.get
  def getParameters: F[Parameters] = parameters.get

  // Returns this node if it ConcreteStateNode and it IO node name and IoIndex in values, otherwise None
  def isInObservedValues(values: Map[Name, IoIndex]): F[Option[ConcreteStateNode[F]]] = this match
    case node: ConcreteStateNode[F] => parameters.get.flatMap: params =>
        values.find((n, i) => node.isBelongsToIo(n, i)) match

          // This node is in observed values, so it should be Plan kind to be moved in context
          case Some((ioName, index)) if params.kind == Kind.Plan => node.some.pure

          case Some((ioName, index)) =>
            (s"Incorrect state of StateNode, expected params.kind == Plan, but found ${params.kind}, " +
              s"for ioName = $ioName and index = $index " +
              s"likely synchronisation bug, this node was moved in to context (i.e. kind changed to " +
              s"past or present) twice").assertionError

          case None => None.pure
    case node: AbstractStateNode[F] => None.pure

  // Scan over THEN children and return set of concrete nodes which was found is in values
  def findThenChildNodesInValues(values: Map[Name, IoIndex]): F[Set[ConcreteStateNode[F]]] =
    structure.get.flatMap(_.thenChildren.toList.traverse(_.isInObservedValues(values))).map(_.flatten.toSet)

  def addLinkChild(node: StateNode[F]): F[Unit] = structure.update(s => s.copy(linkChildren = s.linkChildren + node))
  def addThenChild(node: StateNode[F]): F[Unit] = structure.update(s => s.copy(thenChildren = s.thenChildren + node))

  private def setKind(expectedCurrentKind: Kind, newKind: Kind): F[Unit] = parameters.evalUpdate: params =>
    if params.kind != expectedCurrentKind then
      (s"Incorrect state of StateNode, expected params.kind == $expectedCurrentKind, " +
        s"but found ${params.kind}, likely synchronisation bug").assertionError
    else params.copy(kind = newKind).pure

  def setPresent(): F[Unit] = setKind(Kind.Plan, Kind.Present)
  def setPast(): F[Unit] = setKind(Kind.Present, Kind.Past)

object StateNode:
  enum Kind:
    case Past, Present, Plan

  final case class Structure[F[_]: MonadThrow](
      linkParents: Set[StateNode[F]],
      linkChildren: Set[StateNode[F]],
      thenParents: Set[StateNode[F]],
      thenChildren: Set[StateNode[F]]
  ):
    override def toString: String = s"Structure(\n" +
      s"  linkParents = [${linkParents.mkString(", ")}],\n" +
      s"  linkChildren = [${linkChildren.mkString(", ")}],\n" +
      s"  thenParents = [${thenParents.mkString(", ")}],\n" +
      s"  thenChildren = [${thenChildren.mkString(", ")}]\n" +
      s")"

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
  ):
    override def toString: String = s"Parameters(kind = $kind, observationTime = $observationTime, " +
      s"probability = $probability, utility = $utility)"

  object Parameters:
    lazy val init: Parameters = Parameters(
      kind = Kind.Present,
      observationTime = None,
      probability = 0.0,
      utility = 0.0
    )

  def initState[F[_]: Concurrent](
      linkParents: Set[StateNode[F]],
      thenParents: Set[StateNode[F]],
      initParameters: Parameters
  ): F[(AtomicCell[F, Structure[F]], AtomicCell[F, Parameters])] =
    for
      structure <- AtomicCell[F].of(Structure.init(linkParents, thenParents))
      parameters <- AtomicCell[F].of(initParameters)
    yield (structure, parameters)
