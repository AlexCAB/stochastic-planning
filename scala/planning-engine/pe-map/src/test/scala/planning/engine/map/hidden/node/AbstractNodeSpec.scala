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
| created: 2025-05-15 |||||||||||*/

package planning.engine.map.hidden.node

import cats.effect.IO
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.text.Description
import planning.engine.common.values.node.{HnId, HnName}
import planning.engine.common.properties.PROP
import cats.syntax.all.*
import planning.engine.common.properties.*
import neotypes.model.types.{Node, Value}
import planning.engine.common.validation.ValidationCheck
import planning.engine.common.values.db.Neo4j.{ABSTRACT_LABEL, HN_LABEL}

class AbstractNodeSpec extends UnitSpecWithData with ValidationCheck:

  private class CaseData extends Case:
    lazy val id = HnId(1234)
    lazy val name = HnName.some("TestNode")
    lazy val description = Description.some("TestNodeDescription")
    lazy val singleNode = AbstractNode[IO](id, name, description)
    lazy val newNode = AbstractNode.New(name, description)
    lazy val initNextHnIndex = 1L

    lazy val nodeProperties = Map(
      PROP.HN_ID -> id.toDbParam,
      PROP.NAME -> name.get.toDbParam,
      PROP.DESCRIPTION -> description.get.toDbParam,
      PROP.NEXT_HN_INDEX -> initNextHnIndex.toDbParam
    )

    lazy val nodeValues = Map(
      PROP.HN_ID -> Value.Integer(id.value),
      PROP.NAME -> Value.Str(name.get.value),
      PROP.DESCRIPTION -> Value.Str(description.get.value)
    )

    lazy val rawNode = Node("1", Set(HN_LABEL, ABSTRACT_LABEL), nodeValues)

  "AbstractNode.apply(...)" should:
    "create single AbstractNode with given params" in newCase[CaseData]: (tn, data) =>
      data.singleNode.pure[IO].logValue(tn).asserting: node =>
        node.id mustEqual data.id
        node.name mustEqual data.name
        node.description mustEqual data.description

  "AbstractNode.fromNode(...)" should:
    "create AbstractNode from raw node" in newCase[CaseData]: (_, data) =>
      AbstractNode.fromNode[IO](data.rawNode).asserting: node =>
        node.id mustEqual data.id
        node.name mustEqual data.name
        node.description mustEqual data.description

  "AbstractNode.toProperties(...)" should:
    "make DB node properties" in newCase[CaseData]: (tn, data) =>
      data.newNode.toProperties[IO](data.id, data.initNextHnIndex)
        .logValue(tn).asserting(_ mustEqual data.nodeProperties)

  "AbstractNode.equals(...)" should:
    "return true for same nodes" in newCase[CaseData]: (_, data) =>
      AbstractNode[IO](data.id, data.name, data.description).pure[IO].asserting: node2 =>
        data.singleNode.equals(node2) mustEqual true

    "return false for different nodes" in newCase[CaseData]: (_, data) =>
      AbstractNode[IO](HnId(5678), HnName.some("TestNode2"), None).pure[IO].asserting: node2 =>
        data.singleNode.equals(node2) mustEqual false

  "AbstractNode.validationErrors" should:
    "return empty list for valid new node" in newCase[CaseData]: (tn, data) =>
      data.newNode.checkNoValidationError(tn)

    "return error if name is empty" in newCase[CaseData]: (tn, data) =>
      data.newNode.copy(name = HnName.some("")).checkOneValidationError("Name must not be empty if defined", tn)
