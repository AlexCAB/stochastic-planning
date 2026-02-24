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
| created: 2026-02-16 |||||||||||*/

package planning.engine.planner.map.test.data

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import planning.engine.common.values.edge.{EdgeKey, IndexMap, Indexies}
import planning.engine.common.values.node.{HnIndex, MnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.map.dcg.edges.{DcgEdge, DcgSamples}

trait DcgEdgeTestData:
  private implicit lazy val ioRuntime: IORuntime = IORuntime.global

  def makeDcgEdgeLink(srcId: MnId, trgId: MnId, samples: DcgSamples[IO]): DcgEdge[IO] =
    DcgEdge[IO](EdgeKey.Link(srcId, trgId), samples)

  def makeDcgEdgeThen(srcId: MnId, trgId: MnId, samples: DcgSamples[IO]): DcgEdge[IO] =
    DcgEdge[IO](EdgeKey.Then(srcId, trgId), samples)

  def makeIndexiesMap(sId: SampleId, mnIds: Set[MnId]): IndexMap =
    IndexMap(mnIds.map(id => id -> HnIndex(10000000 + sId.value + id.value)).toMap)

  def makeDcgSamples(sId: SampleId, srcInd: HnIndex, trgInd: HnIndex): DcgSamples[IO] =
    DcgSamples[IO](Map(sId -> Indexies(srcInd, trgInd))).unsafeRunSync()

  def makeDcgSamples(srcId: MnId, trgId: MnId, sampleIds: Iterable[(SampleId, IndexMap)]): DcgSamples[IO] =
    DcgSamples[IO]
      .apply(sampleIds.map((sId, indMap) => sId -> indMap.get[IO](srcId, trgId).unsafeRunSync()).toMap)
      .unsafeRunSync()
  
  def makeDcgEdgeLink(srcId: MnId, trgId: MnId, samples: Iterable[(SampleId, IndexMap)]): DcgEdge[IO] =
    makeDcgEdgeLink(srcId, trgId, makeDcgSamples(srcId, trgId, samples))

  def makeDcgEdgeThen(srcId: MnId, trgId: MnId, samples: Iterable[(SampleId, IndexMap)]): DcgEdge[IO] =
    makeDcgEdgeThen(srcId, trgId, makeDcgSamples(srcId, trgId, samples))

  def makeIndexiesForSampleIds(allMnIds: Set[MnId], ids: SampleId*): Iterable[(SampleId, IndexMap)] =
    ids.map(sId => sId -> makeIndexiesMap(sId, allMnIds))

  lazy val srcCon = MnId.Con(1001)
  lazy val trgAbs = MnId.Abs(1002)

  lazy val keyLink = EdgeKey.Link(srcCon, trgAbs)
  lazy val keyThen = EdgeKey.Then(srcCon, trgAbs)

  lazy val samplesLink: DcgSamples[IO] = makeDcgSamples(SampleId(10), HnIndex(1), HnIndex(1))
  lazy val samplesThen: DcgSamples[IO] = makeDcgSamples(SampleId(20), HnIndex(2), HnIndex(3))

  lazy val dcgEdgeLink = makeDcgEdgeLink(srcCon, trgAbs, samplesLink)
  lazy val dcgEdgeThen = makeDcgEdgeThen(srcCon, trgAbs, samplesThen)
  
  extension (edgeMap: Map[EdgeKey, DcgEdge[IO]])
    def getAllSampleIds: Set[SampleId] = edgeMap.values.flatMap(_.samples.sampleIds).toSet

  extension (samples: DcgSamples[IO])
    def getIndexies(sampleId: SampleId): Indexies = samples.indexies
      .getOrElse(sampleId, throw AssertionError(s"Sample ID ${sampleId.value} not found in DcgSamples"))
    
    def getSrcIndex(sampleId: SampleId): HnIndex = getIndexies(sampleId).src
    def getTrgIndex(sampleId: SampleId): HnIndex = getIndexies(sampleId).trg

