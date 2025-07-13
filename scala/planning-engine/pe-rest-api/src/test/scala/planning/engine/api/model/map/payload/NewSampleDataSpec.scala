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
| created: 2025-07-11 |||||||||||*/

package planning.engine.api.model.map.payload

import cats.effect.IO
import planning.engine.common.UnitSpecWithData
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.text.{Description, Name}
import cats.syntax.all.*
import planning.engine.common.values.node.IoIndex

class NewSampleDataSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    lazy val testConNodeDef = ConcreteNodeDef(Name("hn1"), Name("ioNode1"), IoIndex(0))
    lazy val testAbsNodeDef = AbstractNodeDef(Name("hn2"))

    lazy val testSample: IO[NewSampleData] = NewSampleData(
      probabilityCount = 10,
      utility = 0.5,
      name = Some(Name("sample1")),
      description = Some(Description("Sample 1 description")),
      hiddenNodes = List(testConNodeDef, testAbsNodeDef),
      edges = List(NewSampleEdge(testConNodeDef.name, testAbsNodeDef.name, EdgeType.THEN))
    ).pure[IO]

  "NewSampleData.validationErrors(...)" should:
    "validate correct sample data" in newCase[CaseData]: (_, data) =>
      data.testSample.asserting: sample =>
        sample.validationName mustEqual "NewSampleData"
        sample.validationErrors mustBe empty

    "fail validation for invalid sample data" in newCase[CaseData]: (_, data) =>
      data.testSample.asserting: sample =>
        sample.copy(probabilityCount = -1).validationErrors.head
          .getMessage mustEqual "Probability count must be greater than zero"

        sample.copy(hiddenNodes = List()).validationErrors.head
          .getMessage mustEqual "Hidden nodes names must not be empty"

        sample
          .copy(hiddenNodes =
            List(
              data.testConNodeDef,
              data.testAbsNodeDef.copy(name = data.testConNodeDef.name)
            )
          )
          .validationErrors.head
          .getMessage mustEqual "Hidden nodes names must be unique"

        sample.copy(edges = List()).validationErrors.head
          .getMessage mustEqual "Edges must not be empty"

        sample
          .copy(hiddenNodes = List(data.testConNodeDef))
          .validationErrors.head.getMessage mustEqual "Edges must reference all provided hnNames"
