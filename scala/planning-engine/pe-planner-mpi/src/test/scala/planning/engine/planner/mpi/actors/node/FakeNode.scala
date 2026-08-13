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

import org.apache.pekko.actor.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.node.data.Message.{AddEdgeSrc, AddEdgeTrg}
import planning.engine.planner.mpi.actors.node.logic.ApiImpl
import planning.engine.planner.mpi.common.data.edge.{EdgeData, MeRef}

final case class FakeNode(api: Node, probe: TestProbe[Node.Msg]):
  def expectAddEdgeSrc: (MeRef, EdgeData) =
    val msg = probe.expectMessageType[AddEdgeSrc]
    (msg.ref, msg.data)

  def expectAddEdgeTrg: MeRef = probe.expectMessageType[AddEdgeTrg].ref

object FakeNode:
  def apply(mnId: MnId, name: String = "")(using testKit: ActorTestKit): FakeNode =
    val nodeName = if name.isEmpty then None else Some(HnName(name))
    apply(mnId, nodeName)

  def apply(mnId: MnId, name: Option[HnName])(using testKit: ActorTestKit): FakeNode =
    val safeName = name.map(_.value.replaceAll("[^a-zA-Z0-9\\-_.*$+:@&=,!~';]", "_")).getOrElse("none")
    val probe = testKit.createTestProbe[Node.Msg](s"FakeNodeProbe-id_${mnId.value}-name_$safeName")
    FakeNode(ApiImpl(mnId, name, probe.ref), probe)
