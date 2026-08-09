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
| created: 07-Aug-26 |||||||||||*/

package planning.engine.planner.mpi.actors.manager

import planning.engine.planner.mpi.actors.manager.logic.Actor

trait ManagerLike:
  ??? // TODO Here the manager API definition

class Manager private[manager] (ref: Actor.Ref) extends ManagerLike:
  ??? // TODO Here the manager API implementation

object Manager:
  ??? // TODO Here the manager constructor and factory methods