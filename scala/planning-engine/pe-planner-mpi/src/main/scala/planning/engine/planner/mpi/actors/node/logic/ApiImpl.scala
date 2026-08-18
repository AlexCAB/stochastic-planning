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
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.ApiBase
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.node.data.Message
import planning.engine.planner.mpi.common.data.edge.{EdgeData, MeRef}
import planning.engine.planner.mpi.common.data.samples.Sample

private[node] final case class ApiImpl(mnId: MnId, name: Option[HnName], actor: Actor.Ref)
    extends Node with ApiBase[Actor.Msg]:
  import Message.*

  def upsertEdgeSrc[F[_]: MonadThrow](ref: MeRef, props: Map[SampleId, Sample.Props]): F[Unit] = 
    actor.tellF(UpsertEdgeSrc(ref, props))

  def upsertEdgeTrg[F[_]: MonadThrow](ref: MeRef, props: Map[SampleId, Sample.Props]): F[Unit] = 
    actor.tellF(UpsertEdgeTrg(ref, props))

  override lazy val toString: String = s"Node(id = $mnId, name = ${name.repr}, path = ${actor.path})"
