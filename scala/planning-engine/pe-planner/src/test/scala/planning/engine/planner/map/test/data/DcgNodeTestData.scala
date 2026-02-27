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
| created: 2026-02-16 |||||||||||*/

package planning.engine.planner.map.test.data

import cats.effect.IO
import planning.engine.common.values.io.{IoIndex, IoName, IoValue}
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.text.Description
import planning.engine.planner.map.dcg.nodes.DcgNode

trait DcgNodeTestData extends MapNodeTestData:
  def makeIoValue(name: String = "TestIoVariable", index: Long = 100): IoValue = 
    IoValue(IoName(name), IoIndex(index))
  
  def makeConDcgNode(
      id: MnId.Con = MnId.Con(3000005),
      valueIndex: IoIndex = IoIndex(102)
  ): DcgNode.Concrete[IO] = DcgNode.Concrete[IO](
    id = id,
    name = HnName.some(s"Con DCG Node $id"),
    description = Description.some(s"A concrete DCG node for testing, $id"),
    ioNode = testBoolInNode,
    valueIndex = valueIndex
  )

  def makeAbsDcgNode(id: MnId.Abs = MnId.Abs(3000006)): DcgNode.Abstract[IO] = DcgNode.Abstract[IO](
    id = id,
    name = HnName.some(s"Abs DCG Node $id"),
    description = Description.some(s"An abstract DCG node for testing, $id")
  )

  lazy val mnId1 = MnId.Con(1)
  lazy val mnId2 = MnId.Con(2)
  
  lazy val mnId3 = MnId.Abs(3)
  lazy val mnId4 = MnId.Abs(4)
  lazy val mnId5 = MnId.Abs(5)

  lazy val allConMnId: Set[MnId.Con] = Set(mnId1, mnId2) 
  lazy val allAbsMnId: Set[MnId.Abs] = Set(mnId3, mnId4, mnId5)
  
  lazy val allMnId: Set[MnId] = Set(mnId1, mnId2, mnId3, mnId4, mnId5)

  lazy val conNodes: List[DcgNode.Concrete[IO]] = List(mnId1, mnId2).map(id => makeConDcgNode(id = id))
  lazy val absNodes: List[DcgNode.Abstract[IO]] = List(mnId3, mnId4, mnId5).map(id => makeAbsDcgNode(id = id))
