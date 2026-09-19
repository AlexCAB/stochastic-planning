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

package planning.engine.planner.mpi.actors.node.logic

import cats.MonadThrow
import cats.syntax.all.*
import org.apache.pekko.actor.typed.Scheduler
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.ApiBase
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.node.data.Message
import planning.engine.planner.mpi.model.data.edge.MeRef
import planning.engine.planner.mpi.model.data.node.*
import planning.engine.planner.mpi.model.data.samples.Sample
import planning.engine.common.errors.*
import planning.engine.common.values.io.IoValue

private[node] abstract class ApiImpl(scheduler: Scheduler) extends ApiBase[Actor.Msg](scheduler) with Node:
  import Message.*

  def mnId: MnId
  def name: Option[HnName]
  def actor: Actor.Ref

  override def upsertEdgeSrc[F[_]: MonadThrow](ref: MeRef, props: Map[SampleId, Sample.Props]): F[Unit] =
    actor.tellF(UpsertEdgeSrc(ref, props))

  override def upsertEdgeTrg[F[_]: MonadThrow](ref: MeRef, props: Map[SampleId, Sample.Props]): F[Unit] =
    actor.tellF(UpsertEdgeTrg(ref, props))

  override lazy val toString: String = s"Node(id = $mnId, name = ${name.repr}, path = ${actor.path})"

private[node] object ApiImpl:
  final case class Con(
      mnId: MnId.Con,
      name: Option[HnName],
      ioValue: IoValue,
      actor: Actor.Ref,
      scheduler: Scheduler,
  ) extends ApiImpl(scheduler) with Node.Con

  final case class Abs(
      mnId: MnId.Abs,
      name: Option[HnName],
      actor: Actor.Ref,
      scheduler: Scheduler,
  ) extends ApiImpl(scheduler) with Node.Abs

  def apply[F[_]: MonadThrow](mnId: MnId, data: NodeData, actor: Actor.Ref, scheduler: Scheduler): F[ApiImpl] = 
    (mnId, data) match
      case (mnId: MnId.Con, data: NodeData.Con) => Con(mnId, data.name, data.ioValue, actor, scheduler).pure
      case (mnId: MnId.Abs, data: NodeData.Abs) => Abs(mnId, data.name, actor, scheduler).pure
      case _ => "Invalid combination of MnId and NodeData for ApiImpl creation".assertionError
