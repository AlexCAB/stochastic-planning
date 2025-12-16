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
| created: 2025-12-15 |||||||||||*/

package planning.engine.planner.map

import cats.effect.IO
import cats.effect.cps.*
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.io.{IoIndex, IoValue}

class MapInMemSpec extends UnitSpecWithData with AsyncMockFactory with MapTestData:

  private class CaseData extends Case with SimpleMemStateTestData:
    lazy val notInMap = IoValue(testIntInNode.name, IoIndex(-1))

    val mapInMem = MapInMem[IO]().unsafeRunSync()

  "MapInMem.getForIoValues(...)" should:
    "get nodes for io values from in-memory state" in newCase[CaseData]: (n, data) =>
      async[IO]:
        data.mapInMem.setState(data.dcgState).await
        
        val (foundNodes, notFoundValues) = data.mapInMem
          .getForIoValues(data.ioValues.toSet + data.notInMap)
          .logValue(n).await

        foundNodes mustBe data.dcgNodes
        notFoundValues mustBe Set(data.notInMap)
