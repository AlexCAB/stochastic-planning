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

package planning.engine.map.knowledge.graph

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import neotypes.model.types.Node
import planning.engine.common.values.Name
import planning.engine.map.io.node.{InputNode, OutputNode}
import planning.engine.map.io.variable.BooleanIoVariable
import planning.engine.map.samples.SamplesState
import planning.engine.map.database.Neo4jQueries.*

trait KnowledgeGraphTestData:
  self: AsyncIOSpec =>

  protected lazy val testMetadata = Metadata("TestMap", "Test description")
  protected lazy val emptySamplesState = SamplesState.empty
  protected lazy val emptyNeo4jNode = Node("test_res_node", Set(), Map())

  protected lazy val boolInNode =
    InputNode[IO](Name("inputNode"), BooleanIoVariable[IO](Set(true, false))).unsafeRunSync()

  protected lazy val boolOutNode =
    OutputNode[IO](Name("outputNode"), BooleanIoVariable[IO](Set(true, false))).unsafeRunSync()

  protected lazy val allRootNodeLabels = Set(ROOT_LABEL, SAMPLES_LABEL, IO_NODES_LABEL, IO_NODE_LABEL)
  protected lazy val allIoNodeTypes = Set(InputNode.IN_NODE_TYPE, OutputNode.OUT_NODE_TYPE)
  protected lazy val allIoNodes = Set(boolInNode, boolOutNode)
