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

import cats.Monad
import cats.syntax.all.*
import planning.engine.planner.mpi.actors.planner.data.state.*
import planning.engine.planner.mpi.repr.Representable

private[planner] final case class State(
    // Contain information related to map network structure and connectivity.
    struct: Struct,

    // Current plan graph data
    tick: Tick,
) extends Representable:
  def mapStruct(f: Struct => Struct): State = copy(struct = f(struct))
  def mapStructF[F[_]: Monad](f: Struct => F[Struct]): F[State] = f(struct).map(ns => copy(struct = ns))
  
  def mapTickF[F[_]: Monad](f: Tick => F[Tick]): F[State] = f(tick).map(nt => copy(tick = nt))
  def mapTick(f: Tick => Tick): State = copy(tick = f(tick))

private[planner] object State:
  val init: State = State(Struct.init, Tick.init)
