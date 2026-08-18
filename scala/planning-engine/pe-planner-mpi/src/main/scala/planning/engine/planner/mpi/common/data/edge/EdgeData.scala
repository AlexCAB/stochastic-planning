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
//| created: 12.06.2026 |||||||||||*/
//
//package planning.engine.planner.mpi.common.data.edge
//
//import cats.MonadThrow
//import cats.syntax.all.*
//import planning.engine.common.graph.edges.{Indexies, MeKey}
//import planning.engine.common.values.sample.SampleId
//import planning.engine.common.errors.*
//
//final case class EdgeData(indexies: Map[SampleId, Indexies]):
//  def join[F[_]: MonadThrow](other: EdgeData): F[EdgeData] =
//    for
//        _ <- indexies.keySet.assertContainsNoneOf(other.indexies.keySet, "EdgeData.join: duplicate sample IDs found")
//    yield EdgeData(this.indexies ++ other.indexies)
//
//object EdgeData:
//  final case class Kit(
//      edges: Map[MeKey, EdgeData],
//  )
//
//  val empty: EdgeData = EdgeData(Map.empty)
