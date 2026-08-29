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
| created: 27.06.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.manager.logic

import cats.syntax.all.*
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.manager.data.Message
import planning.engine.planner.mpi.common.data.edge.MeRef

private[manager] trait Edges:
  self: Actor.type =>
  import Message.*

  protected def upsertEdge[F[_]: S](
      key: MeKey,
      sampleIds: Set[SampleId],
      state: St,
  )(using d: Def, ctx: Ctx): F[MeKey] =
    for
      srcNode <- state.getNode(key.src)
      trgNode <- state.getNode(key.trg)
      samples <- state.getSamples(sampleIds)
      _ <- logInfo(s"[upsertEdge] srcNode = $srcNode, trgNode = $trgNode, samples = $samples")
      _ <- srcNode.upsertEdgeSrc(MeRef(key, srcNode, trgNode), samples.view.mapValues(_.props).toMap)
    yield key

  private[manager] def doAddEdge[F[_]: S](msg: AddEdge, state: St)(using d: Def, ctx: Ctx): F[St] =
    for
      _ <- upsertEdge(msg.key, msg.sampleIds, state)
      _ <- logInfo(s"[AddEdge] Added edge ${msg.key}")
      _ <- d.visualizer.edgesAdded[F](Set(msg.key))
      _ <- msg.reply(EdgeAdded(msg.key))
    yield state
