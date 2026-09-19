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
import planning.engine.planner.mpi.actors.planner.Planner
import planning.engine.planner.mpi.actors.visualizer.Visualizer
import planning.engine.planner.mpi.model.data.node.NodeData
import planning.engine.common.errors.*

private[node] sealed trait Definition:
  def id: MnId
  def data: NodeData
  def actors: Definition.Actors

  def self[F[_]: MonadThrow](using ctx: Actor.Ctx): F[Node] = 
    ApiImpl(id, data, ctx.self, ctx.system.scheduler).map(_.asInstanceOf[Node])

  def checkIdAndNode[F[_]: MonadThrow](id: MnId, node: Node)(using ctx: Actor.Ctx): F[Unit] =
    for
      _ <- id.assertEquals(this.id, "Given ID does not match this node's ID")
      self <- this.self[F]
      _ <- node.assertEquals(self, "Given node does not match this node")
    yield ()

private[node] final case class ConDef(
    id: MnId.Con,
    data: NodeData.Con,
    actors: Definition.Actors,
) extends Definition:
  override lazy val toString: String = s"[${id.reprValue}, ${data.name.repr}]"

private[node] final case class AbsDef(
    id: MnId.Abs,
    data: NodeData.Abs,
    actors: Definition.Actors,
) extends Definition:
  override lazy val toString: String = s"(${id.reprValue}, ${data.name.repr})"

object Definition:
  final case class Actors(
      manager: Manager,
      visualizer: Option[Visualizer],
      planner: Planner,
  )

  def apply[F[_]: MonadThrow](id: MnId, data: NodeData, actors: Actors): F[Definition] = (id, data) match
    case (id: MnId.Con, data: NodeData.Con) => ConDef(id, data, actors).pure[F]
    case (id: MnId.Abs, data: NodeData.Abs) => AbsDef(id, data, actors).pure[F]
    case _                                  => "Invalid combination of id and data".assertionError
