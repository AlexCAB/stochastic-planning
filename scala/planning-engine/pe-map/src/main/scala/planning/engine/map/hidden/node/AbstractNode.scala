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
import planning.engine.common.values.text.Name
import planning.engine.common.values.node.HnId
import planning.engine.map.hidden.state.node.HiddenNodeState
import planning.engine.common.properties.*
import neotypes.query.QueryArg.Param

class AbstractNode[F[_]: MonadThrow](
    val id: HnId,
    val name: Option[Name],
    state: AtomicCell[F, HiddenNodeState[F]]
) extends HiddenNode[F]:

  override def toString: String = s"AbstractHiddenNode(id=$id, name=$name)"

object AbstractNode:
  def apply[F[_]: Concurrent](id: HnId, name: Option[Name], initState: HiddenNodeState[F]): F[AbstractNode[F]] =
    for
      state <- AtomicCell[F].of[HiddenNodeState[F]](initState)
      node = new AbstractNode[F](id, name, state)
    yield node

  def makeDbParams[F[_]: Concurrent](id: HnId, name: Option[Name]): F[Map[String, Param]] = paramsOf(
    PROP_NAME.HN_ID -> id.toDbParam,
    PROP_NAME.NAME -> name.map(_.toDbParam)
  )
