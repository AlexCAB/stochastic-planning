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
| created: 12.06.2026 |||||||||||*/



package planning.engine.planner.mpi.data.edge

import planning.engine.common.graph.edges.Indexies
import planning.engine.common.values.sample.SampleId

final case class EdgeData(indexies: Map[SampleId, Indexies])
  
