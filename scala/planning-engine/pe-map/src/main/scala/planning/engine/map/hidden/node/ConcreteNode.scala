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
import neotypes.query.QueryArg.Param
import planning.engine.map.hidden.state.node.{InitState, NodeState}
import planning.engine.map.io.node.IoNode
import planning.engine.common.properties.*
import planning.engine.common.values.name.OpName
import planning.engine.common.values.node.hidden.HnId
import planning.engine.common.values.node.io.IoValueIndex

class ConcreteNode[F[_]: MonadThrow](
                                      val id: HnId,
                                      val name: OpName,
                                      val valueIndex: IoValueIndex,
                                      state: AtomicCell[F, NodeState],
                                      value: Any, // Used only for visualisation
                                      ioNode: IoNode[F]
) extends HiddenNode[F]:

  override def toString: String =
    s"ConcreteHiddenNode(id=$id, name=$name, valueIndex=$valueIndex, value=$value, ioNode=$ioNode)"

object ConcreteNode:
  def apply[F[_]: Concurrent](
                               id: HnId,
                               name: OpName,
                               sampleIndex: IoValueIndex,
                               ioValueIndex: IoValueIndex,
                               ioNode: IoNode[F]
  ): F[ConcreteNode[F]] =
    for
      value <- ioNode.variable.valueForIndex(ioValueIndex)
      state <- AtomicCell[F].of[NodeState](InitState)
      node = new ConcreteNode[F](id, name, ioValueIndex, state, value, ioNode)
      _ <- ioNode.addConcreteNode(node)
    yield node

  def makeParameters[F[_]: Concurrent](id: HnId, name: OpName, ioValueIndex: IoValueIndex): F[Map[String, Param]] =
    paramsOf(
      PROP_NAME.ID -> id.toDbParam,
      PROP_NAME.NAME -> name.toDbParam,
      PROP_NAME.IO_VALUE_INDEX -> ioValueIndex.toDbParam
    )
