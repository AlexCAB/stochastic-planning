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

package planning.engine.planner.mpi.actors.planner.logic

import planning.engine.planner.mpi.actors.planner.data.Message.Step

// Synchronous planner have 2 key concepts (compared to asynchronous):
//   1. Each step is strictly bound to world time (step time), so plan path locking like:
//      (observation1 at time t) -> (action1 at time t) -> (observation2 at time t+1) -> (action2 at time t+1) -> ...
//      This is mean that if agend got observation1 at time t, it expects observation2 at time t+1, and
//      in case observation2 not observed then whole plan path become invalid.
//      (While asynchronous planner use fuzzy time bounding, so deviation
//      from expected t+1 just make plan path less probable).
//   2. Interaction between agent and world are synchronous, i.e. after receiving observation agent
//      will run planning until action is found (no new observation expected during the planing,
//      which mean world should not change until action executed).
//      (While asynchronous planner receiving stream of observations and run planning continuously and
//      produce stream of actions)
// Synchronous planner is good for tasks with synchronous IO, like chat-based agents,
// where each observation is a message from user and each action is a message to user.
// Asynchronous planner is good for application with asynchronous IO, like robotic/automotive agents,
// where each observation is a sensor reading and each action is a motor command.
private[planner] trait SimpleSyncPlanner:
  self: Actor.type =>

  // TODO
  private[planner] def doStep[F[_]: S](msg: Step, state: St)(using Def, Ctx): F[St] = ???
