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
//| created: 2025-03-22 |||||||||||*/
//
//
//package planning.engine.common.properties
//
//import cats.ApplicativeThrow
//import cats.effect.IO
//import cats.effect.cps.async
//import cats.effect.testing.specs2.CatsEffect
//import org.specs2.mutable.Specification
//import neotypes.model.types.Value
//import cats.effect.cps.*
//
//
//class PropertiesExtensionsSpec extends Specification with CatsEffect:
//  
//  "getForKey" should:
//    "return the parsed value if the key exists" in:
//      val properties = Map("key1" -> Value.Str("value1"))
//      
//      async[IO]:
//        val result = properties
//          .getForKey[IO, String]("key1"):
//            case Value.Str(str) => IO.pure(str)
//            case _ => IO.raiseError(new AssertionError("Expected a string value"))
//          .await
//        
//        result must beEqualTo("value1")
//    
//    "raise an AssertionError if the key does not exist" in:
////      val properties = Map("key1" -> Value.StringValue("value1"))
////      val result: IO[String] = properties.getForKey[IO, String]("key2")(v => IO.pure(v.asString))
////      assertThrowsAssertionError(result, "Missing property 'key2' in properties: Map(key1 -> StringValue(value1))")
////    }
////  }
////
////  "getList" should {
////    "return the parsed list if the key exists and is a list" in {
////      val properties = Map("key1" -> Value.ListValue(List(Value.StringValue("value1"), Value.StringValue("value2"))))
////      val result: IO[List[String]] = properties.getList[IO, String]("key1")(v => IO.pure(v.asString))
////      result.unsafeRunSync() must beEqualTo(List("value1", "value2"))
////    }
////
////    "raise an AssertionError if the key exists but is not a list" in {
////      val properties = Map("key1" -> Value.StringValue("value1"))
////      val result: IO[List[String]] = properties.getList[IO, String]("key1")(v => IO.pure(v.asString))
////      assertThrowsAssertionError(result, "Expected a list value, but got: StringValue(value1)")
////    }
////  }
////
////  "getNumber" should {
////    "return the parsed int value if the key exists and is an int" in {
////      val properties = Map("key1" -> Value.Integer(42))
////      val result: IO[Int] = properties.getNumber[IO, Int]("key1")
////      result.unsafeRunSync() must beEqualTo(42)
////    }
////
////    "return the parsed float value if the key exists and is a float" in {
////      val properties = Map("key1" -> Value.Decimal(3.14))
////      val result: IO[Float] = properties.getNumber[IO, Float]("key1")
////      result.unsafeRunSync() must beEqualTo(3.14f)
////    }
////
////    "raise an AssertionError if the key exists but is not a number" in {
////      val properties = Map("key1" -> Value.StringValue("value1"))
////      val result: IO[Int] = properties.getNumber[IO, Int]("key1")
////      assertThrowsAssertionError(result, "Expected a int value, but got: StringValue(value1)")
////    }
