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
//| created: 2026-10-26 |||||||||||*/
//
//package planning.engine.planner.mpi.model.data.visualization
//
//import planning.engine.common.enums.EdgeType
//import planning.engine.common.graph.edges.MeKey
//import planning.engine.common.values.node.{HnName, MnId}
//import planning.engine.common.values.sample.SampleId
//import planning.engine.common.values.text.{Description, Name}
//import planning.engine.planner.mpi.model.data.node.NodeData
//import planning.engine.planner.mpi.model.data.samples.Sample
//
//final case class SampleViz(
//    id: SampleId,
//    props: Sample.Props,
//    info: Option[Sample.Info],
//    nodes: Map[MnId, NodeData],
//    edges: Set[MeKey],
//):
//  lazy val mnId: Set[MnId] = nodes.keySet
//
//object SampleViz:
//  def apply(sample: Sample): SampleViz = SampleViz(
//    id = sample.id,
//    props = sample.props,
//    info = sample.info,
//    nodes = sample.nodes.map((mnId, node) => (mnId, node.data)),
//    edges = sample.edges,
//  )
