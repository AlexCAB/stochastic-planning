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
import planning.engine.planner.mpi.actors.node.logic.ApiImpl

final case class FakeNode(api: Node, probe: TestProbe[Node.Msg])

object FakeNode:
  def apply(mnId: MnId, name: String = "")(using testKit: ActorTestKit): FakeNode =
    val nodeName = if name.isEmpty then None else Some(HnName(name))
    apply(mnId, nodeName)

  def apply(mnId: MnId, name: Option[HnName])(using testKit: ActorTestKit): FakeNode =
    val probe = testKit.createTestProbe[Node.Msg](s"FakeNodeProbe,id=${mnId.value},name=${name.repr}")
    FakeNode(ApiImpl(mnId, name, probe.ref), probe)
