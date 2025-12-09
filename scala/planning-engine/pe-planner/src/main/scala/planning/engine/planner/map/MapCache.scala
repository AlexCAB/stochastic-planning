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
| created: 2025-11-30 |||||||||||*/

package planning.engine.planner.map

import cats.MonadThrow
import cats.effect.kernel.Async
import cats.effect.std.AtomicCell
import planning.engine.common.values.io.IoValue
import planning.engine.common.values.node.HnId
import cats.syntax.all.*
import org.typelevel.log4cats.LoggerFactory
import planning.engine.map.MapGraphLake
import planning.engine.planner.map.dcg.nodes.{AbstractMapNode, ConcreteMapNode, MapNode}
import planning.engine.planner.map.MapCache.ConcreteNodes
import planning.engine.common.errors.*
import planning.engine.map.subgraph.MapSubGraph

trait MapCacheLike[F[_]]:
  def loadForIoValues(values: Set[IoValue]): F[Map[IoValue, ConcreteNodes[F]]]

class MapCache[F[_]: {Async, LoggerFactory}](
    mapGraph: MapGraphLake[F],
    concreteSet: AtomicCell[F, Map[IoValue, ConcreteNodes[F]]],
    abstractSet: AtomicCell[F, Map[HnId, AbstractMapNode[F]]]
):
  private[map] def addToConcreteSet[R](setAllLoaded: Boolean)(
      newNodes: Map[IoValue, ConcreteNodes[F]] => F[(Map[IoValue, ConcreteNodes[F]], R)]
  ): F[R] = concreteSet.evalModify: conSet =>
    for
      (nodes, r) <- newNodes(conSet)
      newConSet <- nodes.foldRight(conSet.pure): (nn, csF) =>
        csF.flatMap:
          case cs if cs.contains(nn._1) => cs(nn._1).merge(nn._2).map(ns => cs.updated(nn._1, ns))
          case cs                       => (cs + nn).pure
    yield (newConSet, r)

  private[map] def findFullyLoaded(
      values: Set[IoValue],
      cs: Map[IoValue, ConcreteNodes[F]]
  ): (Map[IoValue, ConcreteNodes[F]], Map[IoValue, ConcreteNodes[F]], Set[IoValue]) =
    val (inSet, notLoaded) = values.partition(cs.contains)
    val (fullyLoaded, partLoaded) = inSet.map(k => k -> cs(k)).toMap.partition((_, v) => v.allLoaded)
    (fullyLoaded, partLoaded, notLoaded)

  private[map] def load(partLoaded: Map[IoValue, ConcreteNodes[F]], notLoaded: Set[IoValue]): F[MapSubGraph[F]] =
    for
      toLoadValues <- (partLoaded.map((k, v) => k -> v.nodes.keySet) ++ notLoaded.map(v => v -> Set[HnId]())).pure
      subGraph <- mapGraph.loadSubgraphForIoValue(toLoadValues)
      _ <- (toLoadValues.values.flatten, subGraph.skippedNodes).assertContainsAll("Superfluous nodes presented")
      _ <- subGraph.abstractNodes.assertEmpty("Abstract nodes should not be loaded when loading by IoValues")
    yield subGraph

  private[map] def loadFromMap(
      partLoaded: Map[IoValue, ConcreteNodes[F]],
      notLoaded: Set[IoValue]
  ): F[Map[IoValue, ConcreteNodes[F]]] =
    if partLoaded.isEmpty && notLoaded.isEmpty then Map[IoValue, ConcreteNodes[F]]().pure
    else
      for
        subGraph <- load(partLoaded, notLoaded)
        newNodes <- subGraph.concreteNodes.traverse(ConcreteMapNode.apply).map(ConcreteNodes.init(allLoaded = true))

        // TODO:

      yield newNodes

  def getForIoValues(values: Set[IoValue]): F[(Map[IoValue, ConcreteNodes[F]], Set[IoValue])] =
    addToConcreteSet(setAllLoaded = true): cs =>
      for
        (fullyLoaded, partLoaded, notLoaded) <- findFullyLoaded(values, cs).pure
        newNodes <- loadFromMap(partLoaded, notLoaded)
        _ <- (fullyLoaded.keys, newNodes.keys).assertNoSameElems("Loaded nodes should not be re-loaded")
        allFoundNodes = fullyLoaded ++ newNodes
        ioValueNotInMap = values.filterNot(k => allFoundNodes.contains(k))
      yield (newNodes, (allFoundNodes, ioValueNotInMap))

object MapCache:
  final case class ConcreteNodes[F[_]: MonadThrow](
      nodes: Map[HnId, ConcreteMapNode[F]],
      allLoaded: Boolean
  ):
    def merge(other: ConcreteNodes[F]): F[ConcreteNodes[F]] =
      val hnIdDiff = nodes.keySet.intersect(other.nodes.keySet)
      if hnIdDiff.isEmpty then
        copy(nodes = nodes ++ other.nodes, allLoaded = other.allLoaded).pure
      else
        s"Cannot merge ConcreteNodes with overlapping node IDs: $hnIdDiff".assertionError

  object ConcreteNodes:
    def init[F[_]: MonadThrow](allLoaded: Boolean)(ns: List[ConcreteMapNode[F]]): Map[IoValue, ConcreteNodes[F]] =
      ns.groupBy(_.ioValue).view.mapValues(nl => ConcreteNodes(nl.map(n => n.id -> n).toMap, allLoaded)).toMap

  def apply[F[_]: {Async, LoggerFactory}](mapGraph: MapGraphLake[F]): F[MapCache[F]] =
    for
      concreteSet <- AtomicCell[F].of(Map.empty[IoValue, ConcreteNodes[F]])
      abstractSet <- AtomicCell[F].of(Map.empty[HnId, AbstractMapNode[F]])
    yield new MapCache(mapGraph, concreteSet, abstractSet)
