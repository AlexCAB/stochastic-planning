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

package planning.engine.planner.mpi.actors.node

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import org.apache.pekko.actor.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import planning.engine.common.values.io.{IoIndex, IoName, IoValue}
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.node.data.Message.{UpsertEdgeSrc, UpsertEdgeTrg}
import planning.engine.planner.mpi.actors.node.logic.ApiImpl
import planning.engine.planner.mpi.common.data.edge.MeRef
import planning.engine.planner.mpi.common.data.node.NodeData
import planning.engine.planner.mpi.common.data.samples.Sample
import org.scalatest.matchers.must.Matchers.fail

final case class FakeNode(api: Node, probe: TestProbe[Node.Msg]):
  def expectUpsertEdgeSrc: (MeRef, Map[SampleId, Sample.Props]) =
    val msg = probe.expectMessageType[UpsertEdgeSrc]
    (msg.ref, msg.props)

  def expectUpsertEdgeTrg: (MeRef, Map[SampleId, Sample.Props]) =
    val msg = probe.expectMessageType[UpsertEdgeTrg]
    (msg.ref, msg.props)

object FakeNode:
  private def makeData(mnId: MnId, name: Option[HnName], ioValue: Option[IoValue]): NodeData = (mnId, ioValue) match
    case (mnId: MnId.Con, Some(ioVal)) => NodeData.Con(name, None, ioVal.name, ioVal.index)
    case (mnId: MnId.Con, None)        => NodeData.Con(name, None, IoName("fake-io-node"), IoIndex(0))
    case (mnId: MnId.Abs, None)        => NodeData.Abs(name, None)
    case (mnId, ioVal)                 => fail(s"Unexpected MnId type: $mnId, ioValue = $ioVal")

  def apply(mnId: MnId, name: String = "", ioValue: Option[IoValue] = None)(using ActorTestKit, IORuntime): FakeNode =
    apply(mnId, makeData(mnId, if name.isEmpty then None else Some(HnName(name)), ioValue))

  def apply(mnId: MnId, name: Option[HnName], ioValue: Option[IoValue])(using ActorTestKit, IORuntime): FakeNode =
    apply(mnId, makeData(mnId, name, ioValue))

  def apply(mnId: MnId, data: NodeData)(using testKit: ActorTestKit, rt: IORuntime): FakeNode =
    val safeName = data.name.map(_.value.replaceAll("[^a-zA-Z0-9\\-_.*$+:@&=,!~';]", "_")).getOrElse("none")
    val probe = testKit.createTestProbe[Node.Msg](s"FakeNodeProbe-id_${mnId.value}-name_$safeName")
    val nodeApi = ApiImpl[IO](mnId, data, probe.ref)
    nodeApi.map(api => FakeNode(api, probe)).unsafeRunSync()
