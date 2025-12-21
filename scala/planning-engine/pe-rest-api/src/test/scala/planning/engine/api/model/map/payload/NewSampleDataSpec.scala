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

import planning.engine.common.UnitSpecWithData
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.text.{Description, Name}
import planning.engine.common.values.io.IoName
import io.circe.Json
import planning.engine.common.validation.ValidationCheck
import planning.engine.common.values.node.HnName

class NewSampleDataSpec extends UnitSpecWithData with ValidationCheck:

  private class CaseData extends Case:
    lazy val testConNodeDef =
      ConcreteNodeDef(HnName("hn1"), Description.some("testConNodeDef"), IoName("ioNode1"), Json.fromLong(1234L))

    lazy val testAbsNodeDef = AbstractNodeDef(HnName("hn2"), Description.some("testAbsNodeDef"))

    lazy val testSample: NewSampleData = NewSampleData(
      probabilityCount = 10,
      utility = 0.5,
      name = Name.some("sample1"),
      description = Description.some("Sample 1 description"),
      edges = List(NewSampleEdge(testConNodeDef.name, testAbsNodeDef.name, EdgeType.THEN))
    )

  "NewSampleData.validationErrors(...)" should:
    "validate correct sample data" in newCase[CaseData]: (tn, data) =>
      data.testSample.checkValidationName("NewSampleData(name = Name(sample1))", tn)
      data.testSample.checkNoValidationError(tn)

    "fail validation for invalid sample data" in newCase[CaseData]: (tn, data) =>
      data.testSample.copy(probabilityCount = -1)
        .checkOneValidationError("Probability count must be greater than zero", tn)

      data.testSample.copy(edges = List())
        .checkOneValidationError("Edges must not be empty", tn)
