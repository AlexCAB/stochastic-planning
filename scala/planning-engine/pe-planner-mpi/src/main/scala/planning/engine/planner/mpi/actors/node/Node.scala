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
import planning.engine.common.values.io.IoValue
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.manager.Manager
import planning.engine.planner.mpi.actors.node.data.Definition
import planning.engine.planner.mpi.actors.node.logic.{Actor, ApiImpl}
import planning.engine.planner.mpi.actors.planner.Planner
import planning.engine.planner.mpi.actors.visualizer.Visualizer
import planning.engine.planner.mpi.common.data.edge.MeRef
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

  trait Con extends Node:
    def mnId: MnId.Con
    def name: Option[HnName]
    def ioValue: IoValue

  trait Abs extends Node:
    def mnId: MnId.Abs
    def name: Option[HnName]

  def spawn[F[_]: MonadThrow](
      mnId: MnId,
      data: NodeData,
      manager: Manager,
      visualizer: Visualizer,
      planner: Planner,
      make: (Behavior[Msg], String) => ActorRef[Msg],
  ): F[Node] =
    for
      definition <- Definition(mnId, data, Definition.Actors(manager, visualizer, planner))
      api <- ApiImpl(mnId, data, Actor.spawn(definition, make))
    yield api
