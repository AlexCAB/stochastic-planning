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
| created: 2026-10-08 |||||||||||*/

package planning.engine.api.service.visualization.mpi

import cats.effect.cps.*
import cats.effect.std.Dispatcher
import cats.effect.{IO, Resource}
import fs2.Stream
import fs2.concurrent.Topic
import planning.engine.api.model.map.TestMpiData
import planning.engine.api.model.visualization.VisualizationMsg
import planning.engine.api.model.visualization.mpi.{MpiInit, MpiNewEdges, MpiNewNodes}
import planning.engine.common.UnitSpecWithResource
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.io.IoIndex
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.text.{Description, Name}
import planning.engine.planner.mpi.model.data.map.Metadata
import planning.engine.planner.mpi.model.data.node.NodeData

class VisualizationMpiServiceSpec
    extends UnitSpecWithResource[(VisualizationMpiService[IO], Topic[IO, VisualizationMsg])] with TestMpiData:

  override val resource: Resource[IO, (VisualizationMpiService[IO], Topic[IO, VisualizationMsg])] =
    for
      topic <- Resource.eval(Topic[IO, VisualizationMsg])
      dispatcher <- Dispatcher.sequential[IO](await = true)
    yield (new VisualizationMpiService[IO](topic, dispatcher), topic)

  val testMetadata: Metadata = Metadata(Name("test-map"), Description.some("Test map description"))

  val conId: MnId.Con = MnId.Con(1L)
  val absId: MnId.Abs = MnId.Abs(2L)

  val conData: NodeData = NodeData.Con(Some(HnName("con")), None, boolVar.name, IoIndex(1L))
  val absData: NodeData = NodeData.Abs(Some(HnName("abs")), None)

  val testIds: Map[MnId, NodeData] = Map(conId -> conData, absId -> absData)
  val testKeys: Set[MeKey] = Set(MeKey.Link(conId, absId))

  def receive(service: VisualizationMpiService[IO], topic: Topic[IO, VisualizationMsg], n: Int)(
      publish: IO[Unit],
  ): IO[List[VisualizationMsg]] =
    for
      fiber <- service.mapSendWs.take(n.toLong).compile.toList.start
      _ <- topic.subscribers.find(_ > 0).compile.drain
      _ <- publish
      messages <- fiber.joinWithNever
    yield messages

  "VisualizationMpiService.init(...)" should:
    "publish MpiInit message built from metadata and variables" in: (service, topic) =>
      async[IO]:
        val expected = MpiInit[IO](testMetadata, vars).await
        val messages = receive(service, topic, 1)(service.init[IO](testMetadata, vars)).logValue("init").await

        messages mustBe List(expected)

  "VisualizationMpiService.nodesAdded(...)" should:
    "publish MpiNewNodes message built from added nodes" in: (service, topic) =>
      async[IO]:
        val expected = MpiNewNodes[IO](testIds).await
        val messages = receive(service, topic, 1)(service.nodesAdded[IO](testIds)).logValue("nodesAdded").await

        messages mustBe List(expected)

    "fail when node ID type doesn't match node data type" in: (service, _) =>
      service.nodesAdded[IO](Map(conId -> absData)).logValue("nodesAdded")
        .assertThrowsError[AssertionError](_.getMessage must include("Unexpected MnId type"))

  "VisualizationMpiService.edgesAdded(...)" should:
    "publish MpiNewEdges message built from added edge keys" in: (service, topic) =>
      async[IO]:
        val expected = MpiNewEdges.formMeKeys[IO](testKeys).await
        val messages = receive(service, topic, 1)(service.edgesAdded[IO](testKeys)).logValue("edgesAdded").await

        messages mustBe List(expected)

  "VisualizationMpiService.mapSendWs" should:
    "deliver messages in the order they were published" in: (service, topic) =>
      async[IO]:
        val expectedInit = MpiInit[IO](testMetadata, vars).await
        val expectedNodes = MpiNewNodes[IO](testIds).await
        val expectedEdges = MpiNewEdges.formMeKeys[IO](testKeys).await

        val publishAll =
          for
            _ <- service.init[IO](testMetadata, vars)
            _ <- service.nodesAdded[IO](testIds)
            _ <- service.edgesAdded[IO](testKeys)
          yield ()

        val messages = receive(service, topic, 3)(publishAll).logValue("mapSendWs").await

        messages mustBe List(expectedInit, expectedNodes, expectedEdges)

  "VisualizationMpiService.mapReceiveWs" should:
    "log received ping messages" in: (service, _) =>
      service.mapReceiveWs(Stream.emit("test-ping-msg")).compile.drain.asserting(_ => succeed)

  "VisualizationMpiService.apply()" should:
    "create service as a resource" in: (_, _) =>
      VisualizationMpiService[IO]().use(service => service.mapReceiveWs(Stream.empty).compile.drain).logValue("apply")
        .asserting(_ => succeed)
