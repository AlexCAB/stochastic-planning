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
import planning.engine.common.values.node.HnIndex
import planning.engine.common.values.text.{Description, Name}
import planning.engine.map.io.node.{InputNode, OutputNode}
import planning.engine.map.io.variable.BooleanIoVariable
import planning.engine.map.samples.SamplesState
import planning.engine.map.database.Neo4jQueries.*
import planning.engine.map.hidden.state.node.HiddenNodeState

trait KnowledgeGraphTestData:
  self: AsyncIOSpec =>

  protected lazy val testMetadata = MapMetadata(Some(Name("TestMap")), Some(Description("Test description")))
  protected lazy val emptySamplesState = SamplesState.empty
  protected lazy val emptyGraphState = MapCacheState.empty[IO]
  protected lazy val emptyNeo4jNode = Node("test_res_node", Set(), Map())

  protected lazy val boolInNode =
    InputNode[IO](Name("inputNode"), BooleanIoVariable[IO](Set(true, false))).unsafeRunSync()

  protected lazy val boolOutNode =
    OutputNode[IO](Name("outputNode"), BooleanIoVariable[IO](Set(true, false))).unsafeRunSync()

  protected lazy val allRootNodeLabels = Set(ROOT_LABEL, SAMPLES_LABEL, IO_NODES_LABEL, IN_LABEL, OUT_LABEL)
  protected lazy val allIoNodes = Set(boolInNode, boolOutNode)

  protected lazy val initHiddenNodeState = HiddenNodeState.init[IO]
  
  protected lazy val nonEmptyHiddenNodeState = HiddenNodeState[IO](
    List.empty,
    List.empty,
    HnIndex(1234),
    numberOfUsages = 1L
  )
   