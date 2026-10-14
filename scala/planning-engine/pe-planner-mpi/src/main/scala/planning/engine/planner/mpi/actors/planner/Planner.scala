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

package planning.engine.planner.mpi.actors.planner

import cats.effect.Async
import cats.syntax.all.*
import cats.syntax.ext.*
import org.apache.pekko.actor.typed.Scheduler
import org.apache.pekko.actor.typed.scaladsl.ActorContext
import planning.engine.common.graph.io.{Action, Observation}
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.planner.data.Definition
import planning.engine.planner.mpi.actors.planner.logic.{Actor, ApiImpl}
import planning.engine.planner.mpi.model.data.node.StepKey
import planning.engine.planner.mpi.model.io.IoVars

private[mpi] trait Planner:
  // Notify the planner that a new concrete node was added to the map network.
  def conNodesAdded[F[_]: MT](nodes: Set[Node.Con]): F[Unit]

  // Compute the next action for the given observation:
  //  - This is synchronous implementation of the planning (see `SimpleSyncPlanner` trait), so it:
  //      1. Assumes that world is waiting until planning of next step will be done.
  //      2. Assumes world is sequential (no parallel processes allowed).
  //    Which is not the case in the real systems, but for the first implementation it is enough.
  //  - For the simplicity of the implementation and research, a planning process is split into phases,
  //    which are executed sequentially. So each can be explored and changed separately.
  //    The real planning process, will be asynchronous and parallel stream of messages.
  // Processing of the step can be split into several phases:
  //  1. Find set of concrete nodes base on observed IO values.
  //  2. Activation (find active abstract forest, starting from found concrete nodes):
  //       2.1. Planner sends `Activation` message to each found concrete node.
  //       2.2. `Activation` message propagated from concrete nodes to the highest abstract nodes,
  //            and collecting active steps (which expected time is current time + 1) in each node.
  //       2.3. Each  highest abstract node (root of the abstract tree), sends `ActiveSteps` message to the planner
  //            with the list of active steps.
  //       2.4. Planer collects all active steps and active nodes (i.e. nodes which contain active steps).
  //  3. Context update (move context window one step forward in plan forest):
  //       3.1. For each active mode, planner sends `ContextMove` message.
  //       3.2. Each active node, send `ContextExtend` message to the target node of each THEN outgoing
  //            edge, and `ContextShrink` message to the source node of the THEN incoming edge.
  //       3.3. On `ContextExtend`, next node generate new `NextSteps` (extending context this way),
  //            and send then to planner with `NextSteps`. Planner aggregates them in to new `NextSteps` set.
  //       3.4. The `ContextShrink` propagated to the roots of sequence trees, and cat them if contest depth
  //            exceed max. Result roots sends to the planner with `RootSteps` message.
  //            Planner aggregates them in to new `RootSteps` set (so it will know all sequence tree roots,
  //            to run inference).
  //  4. Cleanup (remove invalid plan paths, to reduce complexity of the plan forest, at the end of clean up
  //     only path with active steps (found is phase 2) will be left in the plan forest):
  //       4.1. Find list if invalid `Next` steps (the `Next` steps from the previous tick, which was not activated).
  //       4.2. For each node that contain invalid `Next` step, send `PathCleanup` message.
  //       4.3. Each node, on `PathCleanup`, will remove invalid `Next` steps. Then send `PathCut` to the source
  //            node of the THEN incoming edge of each removed `Next` step, and `TreeCut` to the target nodes of
  //            each THEN outgoing edge from this node.
  //       4.4. The `PathCut` message will be propagated back to the path first step (it can be either root step of
  //            the sequence tree or path fork step). In all nodes, steps related to the path will be removed.
  //       4.5. The `TreeCut` message will be propagated to the leaves of the sequence trees, and all steps related to
  //            the tree will be removed.
  //  5. Inference (run inference on the plan forest, to find  P(N) * U(N) distribution over the action set):
  //       5.1. TODO
  //  6. Planning (a plan forest extension):
  //       6.1. TODO
  //  7. Action selection (select the best action to perform):
  //       7.1. TODO
  def tick[F[_]: Async](observation: Observation)(using Scheduler): F[Action]

  // Aggregate active steps that represent the active forest:
  //  1. Updated plan state, with the active steps.
  //  2. Check if all active steps are collected (it is done by tracking a concrete node IDs).
  //  3. If all are collected, then trigger next phase (which is Context Update).
  def activeSteps[F[_]: MT](steps: Set[StepKey]): F[Unit]

  // TODO
  def nextSteps[F[_]: MT](steps: Set[StepKey]): F[Unit]

  // TODO
  def rootSteps[F[_]: MT](steps: Set[StepKey]): F[Unit]

private[mpi] object Planner:
  type Msg = Actor.Msg

  def spawn[F[_]: MT](
      variables: IoVars,
      ctx: ActorContext[?],
  ): F[Planner] = ApiImpl(Actor.spawn(Definition(variables), (b, n) => ctx.spawn(b, n))).pure
