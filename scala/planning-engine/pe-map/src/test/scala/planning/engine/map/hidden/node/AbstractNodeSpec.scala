///*|||||||||||||||||||||||||||||||||
//|| 0 * * * * * * * * * ▲ * * * * ||
//|| * ||||||||||| * ||||||||||| * ||
//|| * ||  * * * * * ||       || 0 ||
//|| * ||||||||||| * ||||||||||| * ||
//|| * * ▲ * * 0|| * ||   (< * * * ||
//|| * ||||||||||| * ||  ||||||||||||
//|| * * * * * * * * *   ||||||||||||
//| author: CAB |||||||||||||||||||||
//| website: github.com/alexcab |||||
//| created: 2025-05-15 |||||||||||*/
//
//package planning.engine.map.hidden.node
//
//import cats.effect.IO
//import org.scalamock.scalatest.AsyncMockFactory
//import planning.engine.common.UnitSpecIO
//import planning.engine.common.values.text.Name
//import planning.engine.common.values.node.HnId
//import planning.engine.common.properties.PROP_NAME
//import cats.syntax.all.*
//import neotypes.model.types.Value
//import planning.engine.map.graph.{KnowledgeGraphLake, KnowledgeGraphTestData}
//
//class AbstractNodeSpec extends UnitSpecIO with AsyncMockFactory with KnowledgeGraphTestData:
//
//  private class CaseData extends Case:
//    val id = HnId(1234)
//    val name = Some(Name("TestNode"))
//    val node = AbstractNode[IO](id, name, nonEmptyHiddenNodeState).unsafeRunSync()
//    val properties = Map(
//      PROP_NAME.HN_ID -> id.toDbParam,
//      PROP_NAME.NAME -> name.get.toDbParam,
//      PROP_NAME.NEXT_HN_INEX -> nonEmptyHiddenNodeState.nextHnIndex.toDbParam
//    )
//
//    val values = Map(
//      PROP_NAME.HN_ID -> Value.Integer(id.value),
//      PROP_NAME.NAME -> Value.Str(name.get.value),
//      PROP_NAME.NEXT_HN_INEX -> Value.Integer(nonEmptyHiddenNodeState.nextHnIndex.value)
//    )
//
//  "apply" should:
//    "create AbstractNode with given state" in newCase[CaseData]: data =>
//      data.node.pure[IO].logValue.asserting: node =>
//        node.id mustEqual data.id
//        node.name mustEqual data.name
//        node.getState.unsafeRunSync() mustEqual nonEmptyHiddenNodeState
//
//    "create AbstractNode with init state" in newCase[CaseData]: data =>
//      AbstractNode[IO](data.id, data.name).logValue.asserting: node =>
//        node.id mustEqual data.id
//        node.name mustEqual data.name
//        node.getState.unsafeRunSync() mustEqual initHiddenNodeState
//
//  "fromProperties" should:
//    "create AbstractNode from properties" in newCase[CaseData]: data =>
//      AbstractNode.fromProperties[IO](data.values).asserting: node =>
//        node.id mustEqual data.id
//        node.name mustEqual data.name
//        node.getState.unsafeRunSync() mustEqual nonEmptyHiddenNodeState
//
//  "init" should:
//    "initialize AbstractNode" in newCase[CaseData]: data =>
//      data.node.init(123.pure).logValue.asserting: (node, res) =>
//        node mustEqual data.node
//        res mustEqual 123
//
//  "remove" should:
//    "remove AbstractNode" in newCase[CaseData]: data =>
//      data.node.remove(123.pure).logValue.asserting: res =>
//        res mustEqual 123
//
//  "toProperties" should:
//    "make DB node properties" in newCase[CaseData]: data =>
//      data.node.toProperties.logValue.asserting: props =>
//        props mustEqual data.properties
//
//  "equals" should:
//    "return true for same nodes" in newCase[CaseData]: data =>
//      AbstractNode[IO](data.id, data.name).asserting: node2 =>
//        data.node.equals(node2) mustEqual true
//
//    "return false for different nodes" in newCase[CaseData]: data =>
//      AbstractNode[IO](HnId(5678), Some(Name("TestNode2"))).asserting: node2 =>
//        data.node.equals(node2) mustEqual false
