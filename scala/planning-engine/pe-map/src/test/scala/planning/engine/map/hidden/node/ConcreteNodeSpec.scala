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
import planning.engine.common.values.node.{HnId, IoIndex}
import planning.engine.common.values.text.Name
import planning.engine.common.properties.PROP
import cats.syntax.all.*
import planning.engine.common.properties.*
import planning.engine.map.io.node.InputNode
import planning.engine.map.io.variable.IntIoVariable
import neotypes.model.types.{Node, Value}
import planning.engine.common.values.db.Neo4j.{CONCRETE_LABEL, HN_LABEL}

class ConcreteNodeSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    lazy val id = HnId(1234L)
    lazy val name = Some(Name("TestNode"))
    lazy val valueIndex = IoIndex(1L)
    lazy val intInNode = InputNode[IO](Name("inputNode"), IntIoVariable[IO](0, 10))
    lazy val singleNode = ConcreteNode[IO](id, name, intInNode, valueIndex)
    lazy val newNode = ConcreteNode.New(name, intInNode.name, valueIndex)
    lazy val initNextHnIndex = 1L

    lazy val nodeProperties = Map(
      PROP.HN_ID -> id.toDbParam,
      PROP.NAME -> name.get.toDbParam,
      PROP.IO_INDEX -> valueIndex.toDbParam,
      PROP.NEXT_HN_INDEX -> initNextHnIndex.toDbParam
    )

    lazy val nodeValues = Map(
      PROP.HN_ID -> Value.Integer(id.value),
      PROP.NAME -> Value.Str(name.get.value),
      PROP.IO_INDEX -> Value.Integer(valueIndex.value)
    )

    lazy val rawNode = Node("1", Set(HN_LABEL, CONCRETE_LABEL), nodeValues)

  "apply" should:
    "create ConcreteNode with given state" in newCase[CaseData]: (tn, data) =>
      data.singleNode.pure[IO].logValue(tn).asserting: node =>
        node.id mustEqual data.id
        node.name mustEqual data.name
        node.ioNode mustEqual data.intInNode
        node.valueIndex mustEqual data.valueIndex

  "fromNode" should:
    "create ConcreteNode from raw node" in newCase[CaseData]: (_, data) =>
      ConcreteNode.fromNode[IO](data.rawNode, data.intInNode).asserting: node =>
        node.id mustEqual data.id
        node.name mustEqual data.name
        node.ioNode mustEqual data.intInNode
        node.valueIndex mustEqual data.valueIndex

  "toProperties" should:
    "make DB node properties" in newCase[CaseData]: (tn, data) =>
      data.newNode.toProperties[IO](data.id, data.initNextHnIndex)
        .logValue(tn).asserting(_ mustEqual data.nodeProperties)

  "equals" should:
    "return true for same nodes" in newCase[CaseData]: (_, data) =>
      ConcreteNode[IO](data.id, data.name, data.intInNode, data.valueIndex).pure[IO].asserting: node2 =>
        data.singleNode.equals(node2) mustEqual true

    "return false for different nodes" in newCase[CaseData]: (_, data) =>
      ConcreteNode[IO](HnId(5678), Some(Name("TestNode2")), data.intInNode, IoIndex(2)).pure[IO].asserting: node2 =>
        data.singleNode.equals(node2) mustEqual false

  "validationErrors" should:
    "return empty list for valid new node" in newCase[CaseData]: (_, data) =>
      data.newNode.validationErrors.pure[IO].asserting(_ mustBe empty)

    "return error if var name is empty" in newCase[CaseData]: (_, data) =>
      data.newNode.copy(name = Some(Name(""))).validationErrors.pure[IO].asserting: errors =>
        errors must have size 1
        errors.head.getMessage mustEqual "Name must not be empty if defined"

    "return error if IO node name is empty" in newCase[CaseData]: (_, data) =>
      data.newNode.copy(ioNodeName = Name("")).validationErrors.pure[IO].asserting: errors =>
        errors must have size 1
        errors.head.getMessage mustEqual "IoNode name must not be empty"
