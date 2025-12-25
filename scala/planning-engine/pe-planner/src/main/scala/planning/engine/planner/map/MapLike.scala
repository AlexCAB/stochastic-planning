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
import planning.engine.common.values.node.{HnId, HnName}
import planning.engine.common.values.sample.SampleId
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import planning.engine.map.io.node.IoNode
import planning.engine.map.samples.sample.Sample
import planning.engine.planner.map.dcg.nodes.ConcreteDcgNode

trait MapLike[F[_]]:
  def getIoNode(name: IoName): F[IoNode[F]]
  def addNewConcreteNodes(nodes: ConcreteNode.ListNew): F[Map[HnId, Option[HnName]]]
  def addNewAbstractNodes(nodes: AbstractNode.ListNew): F[Map[HnId, Option[HnName]]]
  def addNewSamples(samples: Sample.ListNew): F[Map[SampleId, Sample]]
  def findHnIdsByNames(names: Set[HnName]): F[Map[HnName, Set[HnId]]]
  def getForIoValues(values: Set[IoValue]): F[(Map[IoValue, Set[ConcreteDcgNode[F]]], Set[IoValue])]
  def reset(): F[Unit]
