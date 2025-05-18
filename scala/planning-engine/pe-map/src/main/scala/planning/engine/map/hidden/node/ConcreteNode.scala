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
| created: 2025-04-06 |||||||||||*/

package planning.engine.map.hidden.node

import cats.MonadThrow
import cats.effect.kernel.Concurrent
import cats.effect.std.AtomicCell
import cats.syntax.all.*
import neotypes.model.types.Value
import neotypes.query.QueryArg.Param
import planning.engine.map.hidden.state.node.HiddenNodeState
import planning.engine.map.io.node.IoNode
import planning.engine.common.properties.*
import planning.engine.common.values.text.Name
import planning.engine.common.values.node.{HnId, IoIndex}
import planning.engine.map.knowledge.graph.KnowledgeGraphInternal

class ConcreteNode[F[_]: MonadThrow](
    val id: HnId,
    val name: Option[Name],
    val ioNode: IoNode[F],
    val valueIndex: IoIndex,
    protected val nodeState: AtomicCell[F, HiddenNodeState[F]],
 //   protected val knowledgeGraph: KnowledgeGraphInternal[F],
    value: Any // Used only for visualisation

) extends HiddenNode[F]:

  override def toString: String =
    s"ConcreteHiddenNode(id=$id, name=$name, valueIndex=$valueIndex, value=$value, ioNode=$ioNode)"

object ConcreteNode:
  def apply[F[_]: Concurrent](
      id: HnId,
      name: Option[Name],
      ioNode: IoNode[F],
      ioValueIndex: IoIndex,
      initState: HiddenNodeState[F],
    //  knowledgeGraph: KnowledgeGraphInternal[F]
  ): F[ConcreteNode[F]] =
    for
      value <- ioNode.variable.valueForIndex(ioValueIndex)
      state <- AtomicCell[F].of[HiddenNodeState[F]](initState)
      node = new ConcreteNode[F](id, name, ioNode, ioValueIndex, state, knowledgeGraph, value)
      _ <- ioNode.addConcreteNode(node)
    yield node

  def makeDbParams[F[_]: Concurrent](id: HnId, name: Option[Name], ioValueIndex: IoIndex): F[Map[String, Param]] =
    paramsOf(
      PROP_NAME.HN_ID -> id.toDbParam,
      PROP_NAME.NAME -> name.map(_.toDbParam),
      PROP_NAME.IO_INDEX -> ioValueIndex.toDbParam
    )

  def fromProperties[F[_]: MonadThrow](properties: Map[String, Value]): F[ConcreteNode[F]] = ???
