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
| created: 11.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.manager.logic

import cats.MonadThrow
import cats.effect.Async
import cats.syntax.all.*
import org.apache.pekko.actor.typed.ActorSystem
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.io.IoValue
import planning.engine.common.values.node.MnId
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.ApiBase
import planning.engine.planner.mpi.actors.manager.Manager
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.manager.data.Message
import planning.engine.planner.mpi.common.data.edge.EdgeData
import planning.engine.planner.mpi.common.data.node.NodeData
import planning.engine.planner.mpi.common.data.samples.Sample
import planning.engine.planner.mpi.common.repr.Representable

private[manager] final case class ApiImpl(actor: Actor.Ref) extends Manager with ApiBase[Actor.Msg]:
  import Message.*

  def addNode[F[_]: Async](data: NodeData)(using ActorSystem[?]): F[MnId] =
    actor.askF[F, NodeAdded](ref => AddNode(data, ref)).map(_.id)

  def addEdge[F[_]: Async](key: MeKey, data: EdgeData)(using ActorSystem[?]): F[MeKey] =
    actor.askF[F, EdgeAdded](ref => AddEdge(key, data, ref)).map(_.key)

  def addManSamples[F[_]: Async](
      samples: Set[Sample.Man],
      nodes: Map[MnId.Nim, NodeData],
  )(using ActorSystem[?]): F[Map[SampleId, Sample.Man]] =
    actor.askF[F, ManSamplesAdded](ref => AddManSamples(samples, nodes, ref)).map(_.samples)

  def addGenSamples[F[_]: Async](
      samples: Set[Sample.Gen],
      newNodes: Map[MnId.Nim, Option[IoValue]],
  )(using ActorSystem[?]): F[Map[SampleId, Sample.Gen]] =
    actor.askF[F, GenSamplesAdded](ref => AddGenSamples(samples, newNodes, ref)).map(_.samples)

  def reportError[F[_]: MonadThrow](source: Node, msg: Option[Representable], err: Throwable): F[Unit] =
    actor.tellF(NodeActorError(source, msg, err))

  override lazy val toString: String = s"Manager(path = ${actor.path})"
