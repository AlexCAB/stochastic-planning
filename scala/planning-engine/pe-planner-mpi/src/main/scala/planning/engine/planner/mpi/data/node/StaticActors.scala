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
| created: 19.06.2026 |||||||||||*/

package planning.engine.planner.mpi.data.node

import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.actor.typed.scaladsl.ActorContext
import planning.engine.planner.mpi.actors.manager.Manager
import planning.engine.planner.mpi.actors.visualizer.Visualizer

final case class StaticActors(manager: ActorRef[Manager.Message], visualizer: ActorRef[Visualizer.Message])

object StaticActors:
  def apply()(using dfn: Manager.Definition, ctx: ActorContext[Manager.Message]): StaticActors =
    new StaticActors(ctx.self, dfn.visualizer)
