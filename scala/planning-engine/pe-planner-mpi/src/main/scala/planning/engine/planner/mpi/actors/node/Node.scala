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

package planning.engine.planner.mpi.actors.node

import cats.MonadThrow
import cats.syntax.all.*
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.manager.Manager
import planning.engine.planner.mpi.actors.node.data.Definition
import planning.engine.planner.mpi.actors.node.logic.{Actor, ApiImpl}
import planning.engine.planner.mpi.actors.visualizer.Visualizer
import planning.engine.planner.mpi.common.data.edge.{EdgeData, MeRef}
import planning.engine.planner.mpi.common.data.node.NodeData
import planning.engine.planner.mpi.common.data.samples.Sample

trait Node:
  def mnId: MnId
  def name: Option[HnName]

  // Upsert source end of an edge to this node.
  def upsertEdgeSrc[F[_]: MonadThrow](ref: MeRef, props: Map[SampleId, Sample.Props]): F[Unit]

  // Upsert target end of an edge to this node.
  def upsertEdgeTrg[F[_]: MonadThrow](ref: MeRef, props: Map[SampleId, Sample.Props]): F[Unit]

object Node:
  type Msg = Actor.Msg

  def spawn[F[_]: MonadThrow](
      id: MnId,
      data: NodeData,
      manager: Manager,
      visualizer: Visualizer,
      make: (Behavior[Msg], String) => ActorRef[Msg],
  ): F[Node] =
    for
        definition <- Definition(id, data, Definition.Actors(manager, visualizer))
    yield ApiImpl(id, data.name, Actor.spawn(definition, make))
