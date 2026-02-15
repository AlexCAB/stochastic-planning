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
| created: 2025-12-12 |||||||||||*/

package planning.engine.planner.map

import planning.engine.common.values.io.{IoName, IoValue}
import planning.engine.common.values.node.{MnId, HnName}
import planning.engine.common.values.sample.SampleId
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import planning.engine.map.io.node.IoNode
import planning.engine.map.samples.sample.Sample
import planning.engine.planner.map.dcg.nodes.DcgNode
import planning.engine.planner.map.dcg.samples.DcgSample

trait MapLike[F[_]]:

  // Addition methods
  def addNewConcreteNodes(nodes: ConcreteNode.ListNew): F[Map[MnId, Option[HnName]]]
  def addNewAbstractNodes(nodes: AbstractNode.ListNew): F[Map[MnId, Option[HnName]]]
  def addNewSamples(samples: Sample.ListNew): F[Map[SampleId, DcgSample[F]]]

  // Retrieval methods
  def getIoNode(name: IoName): F[IoNode[F]]

  // Lookup methods
  def findHnIdsByNames(names: Set[HnName]): F[Map[HnName, Set[MnId]]]
  def findForIoValues(values: Set[IoValue]): F[(Map[IoValue, Set[DcgNode.Concrete[F]]], Set[IoValue])]
//  def findActiveAbstractForest(conActiveNodeIds: Set[MnId]): F[ActiveAbsDag[F]]

  // Service methods
  def reset(): F[Unit]
