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
| created: 30.06.2026 |||||||||||*/

package planning.engine.planner.mpi.common.data.node

import cats.effect.IO
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.io.{IoIndex, IoName, IoValue}

class NodeDataSpec extends UnitSpecWithData:
  private class CaseData extends Case

  "NodeData.apply(Option[IoValue])" should:
    "return a NodeData.Con with the value's name and index when given Some(IoValue)" in newCase[CaseData]: (_, _) =>
      val ioValue = IoValue(IoName("testIoName"), IoIndex(3))

      IO.pure(NodeData(Some(ioValue)))
        .asserting(_ mustBe NodeData.Con(
          name = None,
          description = None,
          ioName = ioValue.name,
          valueIndex = ioValue.index,
        ))

    "return an NodeData.Abs when given None" in newCase[CaseData]: (_, _) =>
      IO.pure(NodeData(None)).asserting(_ mustBe NodeData.Abs(name = None, description = None))
