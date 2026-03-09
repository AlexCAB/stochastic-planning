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
import planning.engine.common.UnitSpecWithData
import planning.engine.planner.map.dcg.DcgGraph
import planning.engine.planner.map.test.data.AbstractForestTestData

class MapInMemInferenceSpec extends UnitSpecWithData:

  private class CaseData extends Case with AbstractForestTestData:
    lazy val dcgGraph = emptyDcgGraph
      .addTestNodes(all234ConNodes ++ all234AbsNodes)
    
    def mapInference(graph: DcgGraph[IO])

  "MapInMemInference.naiveInferActiveAbsForest" should :
    "return correct ActiveAbsDag" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val initDcgGraph = dcgGraph.addSamples(List(dcg234SampleAllEdges)).unsafeRunSync()
        logInfo(tn, s"Initial DcgGraph:\n${initDcgGraph.repr.await}").await

        

        



        1 mustBe 1
