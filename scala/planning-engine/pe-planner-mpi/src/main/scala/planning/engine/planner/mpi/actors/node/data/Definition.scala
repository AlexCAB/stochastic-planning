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
| created: 09.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.node.data

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.errors.assertionError
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.manager.Manager
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.node.logic.{Actor, ApiImpl}
import planning.engine.planner.mpi.actors.visualizer.Visualizer
import planning.engine.planner.mpi.common.data.node.{AbsData, ConData, NodeData}

private[node] sealed trait Definition:
  def id: MnId
  def data: NodeData
  def actors: Definition.Actors

  def self(using ctx: Actor.Ctx): Node = ApiImpl(id, data.name, ctx.self)

private[node] final case class ConDef(
    id: MnId.Con,
    data: ConData,
    actors: Definition.Actors,
) extends Definition:
  override lazy val toString: String = s"[${id.reprValue}, ${data.name.repr}]"

private[node] final case class AbsDef(
    id: MnId.Abs,
    data: AbsData,
    actors: Definition.Actors,
) extends Definition:
  override lazy val toString: String = s"(${id.reprValue}, ${data.name.repr})"

object Definition:
  final case class Actors(manager: Manager, visualizer: Visualizer)

  def apply[F[_]: MonadThrow](id: MnId, data: NodeData, actors: Actors): F[Definition] = (id, data) match
    case (id: MnId.Con, data: ConData) => ConDef(id, data, actors).pure[F]
    case (id: MnId.Abs, data: AbsData) => AbsDef(id, data, actors).pure[F]
    case _                             => "Invalid combination of id and data".assertionError
