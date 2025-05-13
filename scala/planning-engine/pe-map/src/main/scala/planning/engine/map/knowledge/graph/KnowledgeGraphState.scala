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
| created: 2025-05-11 |||||||||||*/

package planning.engine.map.knowledge.graph

import cats.MonadThrow
import neotypes.model.types.{Node, Value}
import planning.engine.common.properties.*
import neotypes.query.QueryArg.Param
import cats.syntax.all.*
import planning.engine.map.database.Neo4jQueries.ROOT_LABEL
import planning.engine.common.errors.assertionError
import planning.engine.common.values.node.HnId

final case class KnowledgeGraphState(nextHiddenNodeId: HnId):
  def toQueryParams[F[_]: MonadThrow]: F[Map[String, Param]] =
    paramsOf(PROP_NAME.NEXT_HN_ID -> nextHiddenNodeId.toDbParam)

  def increasedHnId: KnowledgeGraphState = this.copy(nextHiddenNodeId = nextHiddenNodeId.increase)

object KnowledgeGraphState:
  def empty: KnowledgeGraphState = KnowledgeGraphState(HnId.init)

  def fromProperties[F[_]: MonadThrow](props: Map[String, Value]): F[KnowledgeGraphState] =
    for
        nextHiddenNodeId <- props.getValue[F, Long](PROP_NAME.NEXT_HN_ID).map(HnId.apply)
    yield KnowledgeGraphState(nextHiddenNodeId)

  def fromNode[F[_]: MonadThrow](node: Node): F[KnowledgeGraphState] = node match
    case Node(_, labels, props) if labels.exists(_.equalsIgnoreCase(ROOT_LABEL)) => fromProperties[F](props)
    case _ => s"Not a root node, $node".assertionError
