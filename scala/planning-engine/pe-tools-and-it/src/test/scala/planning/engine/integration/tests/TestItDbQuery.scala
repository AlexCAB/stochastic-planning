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
| created: 2025-07-05 |||||||||||*/

package planning.engine.integration.tests

import cats.effect.IO
import planning.engine.common.values.db.Neo4j.*
import planning.engine.common.properties.*
import neotypes.syntax.all.*
import planning.engine.common.values.node.HnId
import neotypes.model.types.{Node, Relationship}
import planning.engine.common.values.sample.SampleId
import planning.engine.common.enums.EdgeType

trait TestItDbQuery:
  self: WithItDb =>
  
  def getNextHnId(implicit db: WithItDb.ItDb): IO[Long] =
    c"MATCH (r:#$ROOT_LABEL) RETURN r".singleNode.map(_.getLongProperty(PROP.NEXT_HN_ID))
    
  def getNextSampleId(implicit db: WithItDb.ItDb): IO[Long] =
    c"MATCH (r:#$SAMPLES_LABEL) RETURN r".singleNode.map(_.getLongProperty(PROP.NEXT_SAMPLES_ID))

  def getNextHnIndex(id: HnId)(implicit db: WithItDb.ItDb): IO[Long] =
    c"MATCH (r:#$HN_LABEL {#${PROP.HN_ID}: ${id.value}}) RETURN r"
      .singleNode.map(_.getLongProperty(PROP.NEXT_HN_INDEX))

  def getSampleNode(id: SampleId)(implicit db: WithItDb.ItDb): IO[Node] =
    c"MATCH (:#$SAMPLES_LABEL) --> (node: #$SAMPLE_LABEL {#${PROP.SAMPLE_ID}: ${id.value}}) RETURN node".singleNode

  def getEdge(sId: HnId, tId: HnId, et: EdgeType, eId: String)(implicit db: WithItDb.ItDb): IO[Relationship] =
    c"""
          MATCH (:#$HN_LABEL {#${PROP.HN_ID}: ${sId.value}}
                )-[e:#${et.toLabel}]->(
                :#$HN_LABEL {#${PROP.HN_ID}: ${tId.value}})
          WHERE elementId(e) = $eId
          RETURN e
          """.singleEdge
