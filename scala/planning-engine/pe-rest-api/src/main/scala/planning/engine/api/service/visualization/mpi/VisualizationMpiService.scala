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
| created: 2026-10-07 |||||||||||*/

package planning.engine.api.service.visualization.mpi

import cats.MonadThrow
import cats.effect.{Async, Resource}
import cats.effect.std.Dispatcher
import cats.syntax.all.*
import cats.syntax.ext.MT
import fs2.concurrent.Topic
import fs2.{Pipe, Stream}
import org.typelevel.log4cats.LoggerFactory
import planning.engine.api.model.visualization.VisualizationMsg
import planning.engine.api.model.visualization.mpi.*
import planning.engine.api.service.visualization.VisualizationService
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.Visualization
import planning.engine.planner.mpi.model.data.map.Metadata
import planning.engine.planner.mpi.model.data.node.NodeData
import planning.engine.planner.mpi.model.io.IoVars

class VisualizationMpiService[F[_]: {Async, LoggerFactory}](
    topic: Topic[F, VisualizationMsg],
    dispatcher: Dispatcher[F],
) extends VisualizationService[F] with Visualization:

  private val topicMaxQueued = 100
  private val logger = LoggerFactory[F].getLogger

  // VisualizationService (Web Socket) interface implementation

  override val mapSendWs: Stream[F, VisualizationMsg] = topic
    .subscribe(topicMaxQueued)

  override val mapReceiveWs: Pipe[F, String, Unit] =
    in => in.evalMap(frameIn => logger.info("[MPI] Pong received: " + frameIn))

  // Publish methods

  private def publish(msg: VisualizationMsg): Unit = dispatcher.unsafeRunSync(
    for
      _ <- topic.publish1(msg)
      _ <- logger.info(s"[MPI] Visualization message published: $msg")
    yield (),
  )

  // Visualization interface implementation

  override def init[G[_]: MT](mt: Metadata, vars: IoVars): G[Unit] = MpiInit(mt, vars).map(publish)
  override def nodesAdded[G[_]: MT](ids: Map[MnId, NodeData]): G[Unit] = MpiNewNodes(ids).map(publish)
  override def edgesAdded[G[_]: MT](keys: Set[MeKey]): G[Unit] = MpiNewEdges.formMeKeys(keys).map(publish)

object VisualizationMpiService:
  def apply[F[_]: {Async, LoggerFactory}](): Resource[F, VisualizationMpiService[F]] =
    for
      topic <- Resource.eval(Topic[F, VisualizationMsg])
      dispatcher <- Dispatcher.sequential(await = true)
    yield new VisualizationMpiService(topic, dispatcher)
