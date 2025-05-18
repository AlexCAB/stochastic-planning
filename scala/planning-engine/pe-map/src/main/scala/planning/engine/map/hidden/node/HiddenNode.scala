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
import cats.effect.std.AtomicCell
import planning.engine.common.values.text.Name
import planning.engine.common.values.node.HnId
import planning.engine.map.hidden.state.node.HiddenNodeState
import planning.engine.map.knowledge.graph.KnowledgeGraphInternal
import cats.syntax.all.*
import neotypes.model.types.{Node, Value}
import neotypes.query.QueryArg.Param
import planning.engine.map.database.Neo4jQueries.{ABSTRACT_LABEL, CONCRETE_LABEL, HN_LABEL}
import planning.engine.map.database.model.extensions.is
import planning.engine.common.properties.*
import planning.engine.common.errors.assertionError
import planning.engine.common.values.db.Label

trait HiddenNode[F[_]]:
  def id: HnId
  def name: Option[Name]
  protected def nodeState: AtomicCell[F, HiddenNodeState[F]]

  private[map] def toDbParams[F[_] : MonadThrow]: F[Map[String, Param]]

  private[map] def allocate[N <: HiddenNode[F], R](block: =>F[R]): F[(N, R)] = nodeState.evalModify(state =>
    for
      nextState <- state.increaseNumUsages
      blockResult <- block
    yield (nextState, (this.asInstanceOf[N], blockResult))
  )

  private[map] def release[N <: HiddenNode[F], R](block: =>F[R]): F[(N, R, Boolean)] = nodeState.evalModify(state =>
    for
      (nextState, isZeroUsages) <- state.decreaseNumUsages
      blockResult <- block
    yield (nextState, (this.asInstanceOf[N], blockResult, isZeroUsages))
  )
  
  def getState: F[HiddenNodeState[F]] = nodeState.get

object HiddenNode:
//  private def getLabelAndProps[F[_]: MonadThrow](node: Node): F[(Label, Map[String, Value])] = node match
//    case n if n.is(HN_LABEL) && n.is(CONCRETE_LABEL) => (CONCRETE_LABEL, n.properties).pure
//    case n if n.is(HN_LABEL) && n.is(ABSTRACT_LABEL) => (ABSTRACT_LABEL, n.properties).pure
//    case _                   => s"Node is not a HiddenNode: $node".assertionError

  private[map] def fromNode[F[_]: MonadThrow](dbData: HiddenNodeDbData): F[HiddenNode[F]] =
    node match
      case n if n.is(HN_LABEL) && n.is(CONCRETE_LABEL) => 
        ConcreteNode.fromProperties(n.properties).map(_.asInstanceOf[HiddenNode[F]])
        
      case n if n.is(HN_LABEL) && n.is(ABSTRACT_LABEL) => 
        AbstractNode.fromProperties(n.properties).map(_.asInstanceOf[HiddenNode[F]])
        
      case _                   => s"Node is not a HiddenNode: $node".assertionError
    
    
    
//    getLabelAndProps(node).flatMap((label, props) =>
//      for
//        id <- props.getValue[F, Long](PROP_NAME.HN_ID).map(HnId.apply)
//        name <- props.getOptional[F, String](PROP_NAME.NAME).flatMap(Name.fromString)
//        state <- HiddenNodeState.fromProperties(props)
//      yield new HiddenNode[F]:
//        val id = id
//        val name = name
//        val nodeState = AtomicCell[F].of(state)
//        val knowledgeGraph = knowledgeGraph
//    
//    
//    )
