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
| created: 31.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.planner.data

import cats.MonadThrow
import planning.engine.common.values.io.IoName
import planning.engine.common.values.node.{HnIndex, MnId}
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.common.repr.Representable

private[planner] final case class State(
    // Concrete nodes known to represent each value (HnIndex) of each input variable.
    inputNodes: Map[IoName, Map[HnIndex, Set[Node]]],

    // Concrete node used for each output variable, along with its HnIndex.
    outputNodes: Map[MnId.Con, (HnIndex, Node)],
) extends Representable:
  def withNewConNodes[F[_]: MonadThrow](): F[State] = ???

private[planner] object State:
  val init: State = State(Map.empty, Map.empty)
