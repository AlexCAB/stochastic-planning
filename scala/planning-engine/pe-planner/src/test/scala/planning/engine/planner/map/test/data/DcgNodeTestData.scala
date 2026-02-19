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
import planning.engine.common.values.io.IoIndex
import planning.engine.common.values.node.{MnId, HnName}
import planning.engine.common.values.text.Description
import planning.engine.planner.map.dcg.nodes.DcgNode

trait DcgNodeTestData extends MapNodeTestData:
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

  lazy val hnId1 = MnId.Con(1)
  lazy val hnId2 = MnId.Con(2)
  
  lazy val hnId3 = MnId.Abs(3)
  lazy val hnId4 = MnId.Abs(4)
  lazy val hnId5 = MnId.Abs(5)

  lazy val allConMnId: Set[MnId.Con] = Set(hnId1, hnId2) 
  lazy val allAbsMnId: Set[MnId.Abs] = Set(hnId3, hnId4, hnId5)
  
  lazy val allHnId = Set(hnId1, hnId2, hnId3, hnId4, hnId5)

  lazy val conNodes: List[DcgNode.Concrete[IO]] = List(hnId1, hnId2).map(id => makeConDcgNode(id = id))
  lazy val absNodes: List[DcgNode.Abstract[IO]] = List(hnId3, hnId4, hnId5).map(id => makeAbsDcgNode(id = id))
