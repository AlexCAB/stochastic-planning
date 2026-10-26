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

package planning.engine.planner.mpi.actors.visualizer.logic

import cats.syntax.all.*
import cats.syntax.ext.*
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.io.IoTime
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.ApiBase
import planning.engine.planner.mpi.actors.planner.data.parts.ActiveSteps
import planning.engine.planner.mpi.actors.visualizer.Visualizer
import planning.engine.planner.mpi.actors.visualizer.data.Message
import planning.engine.planner.mpi.model.data.node.NodeData
import planning.engine.planner.mpi.model.data.samples.Sample

private[visualizer] final case class ApiImpl(actor: Actor.Ref) extends ApiBase[Actor.Msg] with Visualizer:
  import Message.*

  override def nodesAdded[F[_]: MT](ids: Map[MnId, NodeData]): F[Unit] = ifNonEmpty((), ids):
    actor.tellF(ShowNodesAdded(ids))

  override def edgesAdded[F[_]: MT](keys: Set[MeKey]): F[Unit] = ifNonEmpty((), keys):
    actor.tellF(ShowEdgesAdded(keys))

  override def samplesAdded[F[_]: MT](samples: Set[Sample]): F[Unit] = ifNonEmpty((), samples):
    actor.tellF(ShowSamplesAdded(samples))

  override def activeStepsFound[F[_]: MT](time: IoTime, steps: ActiveSteps): F[Unit] =
    actor.tellF(ShowActiveStepsFound(time, steps))

  override lazy val toString: String = s"Visualizer(path = ${actor.path})"
