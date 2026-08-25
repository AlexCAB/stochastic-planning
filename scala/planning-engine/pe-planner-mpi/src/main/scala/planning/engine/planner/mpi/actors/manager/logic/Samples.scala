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
| created: 17-Aug-26 |||||||||||*/

package planning.engine.planner.mpi.actors.manager.logic

import cats.syntax.all.*
import planning.engine.common.errors.*
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.MnId
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.manager.data.Message
import planning.engine.planner.mpi.common.data.samples.Sample

private[manager] trait Samples extends Nodes with Edges:
  self: Actor.type =>
  import Message.*, MnId.Nim

  def doAddManSamples[F[_]: S](msg: AddManSamples, state: St)(using d: Def, ctx: Ctx): F[St] =
    def validate: F[Unit] =
      for 
        nodeIds <- msg.nodes.keySet.map(_.asMnId).pure
        inEdgeIds = msg.samples.flatMap(_.edges.flatMap(_.mnIds))
        _ <- inEdgeIds.assertContainsAllOf(nodeIds, "Some MnId used in samples edges not found in nodes map")
      yield ()
      
    def groupedByEdgeKey(sampleMap: Map[SampleId, Sample.Man]): Map[MeKey, Set[SampleId]] =
      sampleMap.toList
        .flatMap((sid, sample) => sample.edges.map(key => key -> sid))
        .groupBy(_._1)
        .map((key, pairs) => key -> pairs.map(_._2).toSet)

    def resolveEdgeKeys(keyMap: Map[MeKey, Set[SampleId]], idsMap: Map[Nim, MnId]): F[List[(MeKey, Set[SampleId])]] =
      keyMap.toList.traverse((k, sIds) => k.resolve(idsMap).map(_ -> sIds))

    for
      _ <- validate
      (nodeMap, stateWithNodes) <- upsertNodesByName(msg.nodes, state)
      (sampleMap, stateWithSamples) <- stateWithNodes.withNewManSamples(msg.samples)
      edgeKeys <- resolveEdgeKeys(groupedByEdgeKey(sampleMap), nodeMap.view.mapValues(_.mnId).toMap)
      addedKeys <- edgeKeys.traverse((key, sIds) => upsertEdge(key, sIds, stateWithSamples))
      _ <- logInfo("[AddManSamples] Added samples", sampleMap)
      _ <- d.visualizer.edgesAdded[F](addedKeys.toSet)
      _ <- msg.reply(ManSamplesAdded(sampleMap))
    yield stateWithSamples

  def doAddGenSamples[F[_]: S](msg: AddGenSamples, state: St)(using d: Def, ctx: Ctx): F[St] = ???
