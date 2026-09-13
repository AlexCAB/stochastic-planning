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
| created: 31.08.26 |||||||||||||*/

package planning.engine.planner.mpi.actors.node.data

import cats.Monad
import cats.syntax.all.*
import planning.engine.planner.mpi.actors.node.data.state.{Plan, Struct}
import planning.engine.planner.mpi.model.repr.Representable

private[node] final case class State(
    // Contain information related to map network structure and connectivity.
    struct: Struct,

    // Current plan graph data
    plan: Plan,
) extends Representable:
  def mapStruct[F[_]: Monad](f: Struct => F[Struct]): F[State] = f(struct).map(ns => copy(struct = ns))
  def mapPlan[F[_]: Monad](f: Plan => F[Plan]): F[State] = f(plan).map(np => copy(plan = np))

private[node] object State:
  def init: State = State(Struct.init, Plan.init)
