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
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.common.data.node.NodeData
import planning.engine.planner.mpi.common.data.samples.Sample

private[manager] trait Samples extends Nodes with Edges:
  self: Actor.type =>
  import Message.*, MnId.Nim

  private def groupedByEdgeKey(sampleMap: Map[SampleId, Sample.New]): Map[MeKey, Set[SampleId]] = sampleMap.toList
    .flatMap((sid, sample) => sample.edges.map(key => key -> sid))
    .groupBy(_._1)
    .map((key, pairs) => key -> pairs.map(_._2).toSet)

  private def addAllEdges[F[_]: S](
      sampleMap: Map[SampleId, Sample.New],
      nodeMap: Map[Nim, Node],
      state: St,
  )(using d: Def, ctx: Ctx): F[Unit] =
    for
      edgeGrouped <- groupedByEdgeKey(sampleMap).pure
      idsMap = nodeMap.view.mapValues(_.mnId).toMap
      edgeKeys <- edgeGrouped.toList.traverse((k, sIds) => k.resolve(idsMap).map(_ -> sIds))
      addedKeys <- edgeKeys.traverse((key, sIds) => upsertEdge(key, sIds, state))
      _ <- d.visualizer.edgesAdded[F](addedKeys.toSet)
    yield ()

  private[manager] def doAddManSamples[F[_]: S](msg: AddManSamples, state: St)(using d: Def, ctx: Ctx): F[St] =
    def validate: F[Unit] =
      for
        (nimIds, mnIds) <- msg.samples.flatMap(_.mnIds).splitNim.pure
        _ <- msg.nodes.keySet.assertContainsNoneOf(nimIds, "Some Nim used in samples edges not found in nodes map")
        _ <- mnIds.assertEmpty("Only MnId.Nim can be used in samples edges, but found some MnId.Abs or MnId.Gen")
      yield ()

    for
      _ <- validate
      (nodeMap, stateWithNodes) <- upsertNodesByName(msg.nodes, state)
      (sampleMap, stateWithSamples) <- stateWithNodes.withNewManSamples(msg.samples)
      _ <- addAllEdges(sampleMap, nodeMap, stateWithSamples)
      _ <- logInfo("[AddManSamples] Added samples", sampleMap)
      _ <- msg.reply(ManSamplesAdded(sampleMap))
    yield stateWithSamples

  private[manager] def doAddGenSamples[F[_]: S](msg: AddGenSamples, state: St)(using d: Def, ctx: Ctx): F[St] =
    def validate: F[Unit] =
      for
        (nimIds, mnIds) <- msg.samples.flatMap(_.mnIds).splitNim.pure
        _ <- msg.newNodes.keySet.assertContainsNoneOf(nimIds, "Some Nim used in edges not found in nodes map")
        _ <- state.nodeRefMap.keySet.assertContainsAllOf(mnIds, "Some MnId used in edges not found in current state")
      yield ()

    for
      _ <- validate
      (newNodeMap, stateWithNodes) <- addNodes(msg.newNodes.view.mapValues(NodeData(_)).toMap, state)
      (sampleMap, stateWithSamples) <- stateWithNodes.withNewGenSamples(msg.samples)
      _ <- addAllEdges(sampleMap, newNodeMap, stateWithSamples)
      _ <- logInfo("[AddGenSamples] Added samples", sampleMap)
      _ <- msg.reply(GenSamplesAdded(sampleMap))
    yield stateWithSamples
