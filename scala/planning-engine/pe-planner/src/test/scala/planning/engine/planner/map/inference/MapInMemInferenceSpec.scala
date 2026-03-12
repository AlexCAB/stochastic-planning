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
| created: 2026-03-09 |||||||||||*/

package planning.engine.planner.map.inference

import cats.effect.IO
import cats.effect.cps.*
import cats.syntax.all.*
import planning.engine.common.UnitSpecWithData
import planning.engine.planner.map.dcg.DcgGraph
import planning.engine.planner.map.state.MapGraphState
import planning.engine.planner.map.test.data.AbstractDagTestData

class MapInMemInferenceSpec extends UnitSpecWithData:

  private class CaseData extends Case with AbstractDagTestData:
    lazy val dcgGraph = emptyDcgGraph
      .addTestNodes(all234ConNodes ++ all234AbsNodes)

    def makeMapInference(graph: DcgGraph[IO]): MapInference[IO] = new MapInference[IO]:
      private[map] override def getMapState: IO[MapGraphState[IO]] = graph.asMapGraphState.pure[IO]

  "MapInMemInference.naiveInferActiveAbsDag" should:
    "return correct ActiveAbsDag" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val initDcgGraph = dcgGraph.addSamples(List(dcg234SampleAllEdges)).unsafeRunSync()
        logInfo(tn, s"\n##### Initial DcgGraph: ${initDcgGraph.repr.await}").await

        val mapInference = makeMapInference(initDcgGraph)

        val singleNode = mapInference.naiveInferActiveAbsDag(Set(c_1p1_101.id)).await
        logInfo(tn, s"\n##### Single node: ${singleNode.repr.await}").await

        singleNode.backwordKeys.srcIds mustBe Set()
        singleNode.graph.conMnId mustBe Set(c_1p1_101.id)
        singleNode.graph.absMnId mustBe Set()

        val pathLike = mapInference.naiveInferActiveAbsDag(Set(c_1c1_111.id)).await
        logInfo(tn, s"\n##### Path like: ${pathLike.repr.await}").await

        pathLike.backwordKeys.srcIds mustBe Set(c_1p1_101.id, a_2p1_201.id, a_3p1_301.id)
        pathLike.graph.conMnId mustBe Set(c_1c1_111.id)
        pathLike.graph.absMnId mustBe Set(a_2c1_211.id, a_3c1_311.id)

        val treeLike = mapInference.naiveInferActiveAbsDag(Set(c_1c2_112.id)).await
        logInfo(tn, s"\n##### Tree like: ${treeLike.repr.await}").await

        treeLike.backwordKeys.srcIds mustBe Set(c_1p2_102.id, c_1c1_111.id, a_2c1_211.id, a_3p1_301.id, a_3c1_311.id)
        treeLike.graph.conMnId mustBe Set(c_1c2_112.id)
        treeLike.graph.absMnId mustBe Set(a_2c2_212.id, a_3c1_311.id, a_3c2_312.id)

        val dagLike = mapInference.naiveInferActiveAbsDag(Set(c_1c1_111.id, c_1c2_112.id)).await
        logInfo(tn, s"\n##### DAG like: ${dagLike.repr.await}").await

        dagLike.backwordKeys.srcIds mustBe Set(
          c_1p1_101.id,
          c_1p2_102.id,
          c_1c1_111.id,
          a_2p1_201.id,
          a_2c1_211.id,
          a_3p1_301.id,
          a_3c1_311.id
        )

        dagLike.graph.conMnId mustBe Set(c_1c1_111.id, c_1c2_112.id)
        dagLike.graph.absMnId mustBe Set(a_2c1_211.id, a_2c2_212.id, a_3c1_311.id, a_3c2_312.id)
