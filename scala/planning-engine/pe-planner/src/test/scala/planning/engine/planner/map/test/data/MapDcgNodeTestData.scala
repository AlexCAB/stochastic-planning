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
import planning.engine.common.values.io.IoIndex
import planning.engine.common.values.node.{HnId, HnName}
import planning.engine.common.values.text.Description
import planning.engine.planner.map.test.data.MapNodeTestData
import planning.engine.planner.map.dcg.nodes.{AbstractDcgNode, ConcreteDcgNode}

trait MapDcgNodeTestData extends MapNodeTestData:
  def makeConcreteDcgNode(
      id: HnId = HnId(3000005),
      valueIndex: IoIndex = IoIndex(102)
  ): ConcreteDcgNode[IO] = ConcreteDcgNode[IO](
    id = id,
    name = HnName.some(s"Con DCG Node $id"),
    description = Description.some(s"A concrete DCG node for testing, $id"),
    ioNode = testBoolInNode,
    valueIndex = valueIndex
  )

  def makeAbstractDcgNode(id: HnId = HnId(3000006)): AbstractDcgNode[IO] = AbstractDcgNode[IO](
    id = id,
    name = HnName.some(s"Abs DCG Node $id"),
    description = Description.some(s"An abstract DCG node for testing, $id")
  )
