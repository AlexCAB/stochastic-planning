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
| created: 2025-05-11 |||||||||||*/

package planning.engine.map.graph

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import neotypes.model.types.Node
import planning.engine.common.values.node.{HnId, HnIndex, IoIndex}
import planning.engine.common.values.text.{Description, Name}
import planning.engine.map.io.node.{InputNode, IoNode, OutputNode}
import planning.engine.map.io.variable.BooleanIoVariable
import planning.engine.map.database.Neo4jQueries.*
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}

trait MapGraphTestData:
  self: AsyncIOSpec =>

  lazy val testMapConfig = MapConfig(maxCacheSize = 10L)
  lazy val testMetadata = MapMetadata(Some(Name("TestMap")), Some(Description("Test description")))
  lazy val emptyGraphState = MapCacheState.init[IO](sampleCount = 0L).unsafeRunSync()
  lazy val emptyNeo4jNode = Node("test_res_node", Set(), Map())

  lazy val boolInNode = InputNode[IO](Name("inputNode"), BooleanIoVariable[IO](Set(true, false))).unsafeRunSync()
  lazy val boolOutNode = OutputNode[IO](Name("outputNode"), BooleanIoVariable[IO](Set(true, false))).unsafeRunSync()

  lazy val allRootNodeLabels = Set(ROOT_LABEL, SAMPLES_LABEL, IO_NODES_LABEL, IN_LABEL, OUT_LABEL)
  lazy val allIoNodes = Set(boolInNode, boolOutNode)

  lazy val testHnIndex = HnIndex(1L)

  def makeAbstractNode(id: Int): AbstractNode[IO] =
    AbstractNode[IO](id = HnId(id), name = Some(Name("Test Node"))).unsafeRunSync()
    
  def makeConcreteNode(index: Long, ioNode: IoNode[IO]): IO[ConcreteNode[IO]] =
    ConcreteNode[IO](HnId(123), Some(Name("test")), ioNode, IoIndex(index))

  lazy val hiddenNodes: List[AbstractNode[IO]] = (1 to 10).map(i => makeAbstractNode(i)).toList

  def makeInputBoolNode: InputNode[IO] = InputNode[IO]
    .apply(Name("inputNode"), BooleanIoVariable[IO](Set(true, false)))
    .unsafeRunSync()
