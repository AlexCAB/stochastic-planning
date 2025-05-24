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
| created: 2025-04-05 |||||||||||*/

package planning.engine.map.hidden.node

import cats.MonadThrow
//import cats.effect.kernel.Concurrent
import planning.engine.common.values.text.Name
import planning.engine.common.values.node.{HnId, HnIndex}
//import planning.engine.map.hidden.state.node.HiddenNodeState
//import cats.syntax.all.*
//import neotypes.model.types.Node
//import neotypes.query.QueryArg.Param
//import planning.engine.map.database.Neo4jQueries.{ABSTRACT_LABEL, CONCRETE_LABEL, HN_LABEL}
//import planning.engine.common.properties.*
//import planning.engine.common.errors.assertionError
//import planning.engine.map.io.node.IoNode
import planning.engine.map.hidden.state.edge.EdgeState

trait HiddenNode[F[_]: MonadThrow]:
  def id: HnId
  def name: Option[Name]
  def parents: List[HiddenNode[F]]
  def children: List[EdgeState[F]]
  def nextHnIndex: HnIndex


//  private[map] def init[R](block: => F[R]): F[(HiddenNode[F], R)]
//  private[map] def remove[R](block: => F[R]): F[R]
//  private[map] def toProperties: F[Map[String, Param]]
//  
//  private[map] def allocate[N <: HiddenNode[F], R](block: => F[R]): F[(N, R)] = nodeState.evalModify(state =>
//    for
//      nextState <- state.increaseNumUsages
//      blockResult <- block
//    yield (nextState, (this.asInstanceOf[N], blockResult))
//  )
//
//  private[map] def release[R](block: (HiddenNode[F], Boolean) => F[R]): F[R] = nodeState.evalModify(state =>
//    for
//      (nextState, isZeroUsages) <- state.decreaseNumUsages
//      blockResult <- block(this, isZeroUsages)
//    yield (nextState, blockResult)
//  )
//
//  private[map] def getState: F[HiddenNodeState[F]] = nodeState.get
//
//object HiddenNode:
//  private[map] def fromNode[F[_]: Concurrent](node: Node, ioNode: Option[IoNode[F]]): F[HiddenNode[F]] =
//    (node, ioNode) match
//      case (n, Some(io)) if n.is(HN_LABEL) && n.is(CONCRETE_LABEL) =>
//        ConcreteNode.fromProperties(n.properties, io).map(_.asInstanceOf[HiddenNode[F]])
//
//      case (n, _) if n.is(HN_LABEL) && n.is(ABSTRACT_LABEL) =>
//        AbstractNode.fromProperties(n.properties).map(_.asInstanceOf[HiddenNode[F]])
//
//      case _ => s"Node is not a HiddenNode: $node".assertionError
