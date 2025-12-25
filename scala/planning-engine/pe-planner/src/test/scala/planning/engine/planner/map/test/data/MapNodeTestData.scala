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
| created: 2025-12-22 |||||||||||*/

package planning.engine.planner.map.test.data

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import planning.engine.common.values.io.{IoIndex, IoName, IoValue}
import planning.engine.common.values.node.{HnId, HnName}
import planning.engine.common.values.text.{Description, Name}
import planning.engine.map.data.MapMetadata
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import planning.engine.map.io.node.{InputNode, OutputNode}
import planning.engine.map.io.variable.{BooleanIoVariable, IntIoVariable}

trait MapNodeTestData:
  private implicit lazy val ioRuntime: IORuntime = IORuntime.global

  lazy val testBoolInNode = InputNode[IO](IoName("boolInputNode"), BooleanIoVariable[IO](Set(true, false)))
  lazy val testIntInNode = InputNode[IO](IoName("intInputNode"), IntIoVariable[IO](0, 10000))
  lazy val boolOutNode = OutputNode[IO](IoName("boolOutputNode"), BooleanIoVariable[IO](Set(true, false)))

  lazy val testInNodes = List(testBoolInNode, testIntInNode)
  lazy val testOutNodes = List(boolOutNode)

  lazy val testIoValue = IoValue(testBoolInNode.name, IoIndex(-2))

  def makeAbstractNode(id: HnId = HnId(3000003)): AbstractNode[IO] = AbstractNode[IO](
    id = id,
    name = HnName.some(s"Abs Node $id"),
    description = Description.some(s"Test Abstract Node, ID $id")
  )

  def makeConcreteNode(id: HnId = HnId(3000004)): ConcreteNode[IO] = ConcreteNode[IO](
    id = id,
    name = HnName.some(s"Con Node $id"),
    description = Description.some(s"Test Concrete Node, ID $id"),
    ioNode = testBoolInNode,
    valueIndex = IoIndex(id.value + 1000)
  )

  lazy val testMetadata = MapMetadata(
    name = Name.some("Test Map"),
    description = Description.some("A map used for testing MapInfoState")
  )
