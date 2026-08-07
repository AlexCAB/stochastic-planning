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
| created: 18.06.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.visualizer

import cats.effect.Sync
import planning.engine.planner.mpi.actors.ActorBase
import planning.engine.planner.mpi.actors.visualizer.data.{Definitions, States}
import planning.engine.planner.mpi.actors.visualizer.logic.Structure

object VisualizerActor extends ActorBase with Definitions with States with Messages with Structure:
  override type Def = Definition
  override type Msg = Message

  override protected type St = State

  val name = "map-visualizer-actor"

  override protected def receive[F[_]: S](msg: Msg, state: St)(using Def, Ctx): F[St] = msg match
    case msg: Structure.Nodes.Added => doNodesAdded(msg, state)
    case msg: Structure.Edges.Added => doEdgesAdded(msg, state)

  override protected def error[F[_]: S](msg: Msg, state: St, err: Throwable)(using Def, Ctx): F[St] =
    ignoreError(msg, state, err)
