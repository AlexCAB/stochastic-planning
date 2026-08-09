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
| created: 09-Aug-26 |||||||||||*/

package planning.engine.planner.mpi.actors.node.data

import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.common.data.node.{AbsData, ConData, NodeData, StaticActors}

sealed trait Definition:
  def id: MnId
  def data: NodeData
  def actors: StaticActors

final case class ConDef(
    id: MnId.Con,
    data: ConData,
    actors: StaticActors,
) extends Definition:
  override lazy val toString: String = s"[${id.reprValue}, ${data.name.repr}]"

final case class AbsDef(
    id: MnId.Abs,
    data: AbsData,
    actors: StaticActors,
) extends Definition:
  override lazy val toString: String = s"(${id.reprValue}, ${data.name.repr})"
