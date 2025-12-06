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
import planning.engine.planner.map.dcg.nodes.{AbstractMapNode, ConcreteMapNode}
import planning.engine.common.errors.assertDistinct
import planning.engine.planner.map.MapCache.ConcreteNodes

trait MapCacheLike[F[_]]:
  def loadForIoValues(values: Set[IoValue]): F[List[ConcreteMapNode[F]]]

class MapCache[F[_]: {Async, LoggerFactory}](
    mapGraph: MapGraphLake[F],
    concreteSet: AtomicCell[F, Map[IoValue, ConcreteNodes[F]]],
    abstractSet: AtomicCell[F, Map[HnId, AbstractMapNode[F]]]
):
  def loadForIoValues(values: Set[IoValue]): F[List[ConcreteMapNode[F]]] = concreteSet.evalModify: cs =>
    for
      (fullyLoaded, toLoadNodes) <- cs.filter((k, _) => values.contains(k)).partition((_, v) => v.allLoaded).pure
      toLoadValues = toLoadNodes.map((k, v) => k -> v.nodes.keySet) ++ (values -- cs.keySet).map(v => v -> Set[HnId]())

     
      _ <- loadedHnId.assertDistinct("Loaded HnIds for IoValues are not distinct")

      cached = cachedKeys.map(k => k -> st.get(k).toList.flatMap(_.values))
      concreteNodes <- load(notCachedKeys)
      loadedNodes <- concreteNodes.traverse(n => ConcreteMapNode.fromMapNode(n.node))
      loaded = loadedNodes.groupBy(n => (n.ioNode.name, n.valueIndex))
      _ <- (loaded.keys, cached.keys).assertNoSameElems("loaded keys overlap with cached keys")
      _ <- (loaded.keys, st.keys).assertNoSameElems("loaded keys overlap with existing cached keys")
    yield (st ++ loaded.map((k, nodes) => k -> nodes.map(n => n.id -> n).toMap), cached ++ loaded)

object MapCache:
  final case class ConcreteNodes[F[_]: MonadThrow](
      nodes: Map[HnId, ConcreteMapNode[F]],
      allLoaded: Boolean
  )

  object ConcreteNodes:
    def empty[F[_]: MonadThrow]: ConcreteNodes[F] = ConcreteNodes(Map.empty, allLoaded = false)

  def apply[F[_]: {Async, LoggerFactory}](mapGraph: MapGraphLake[F]): F[MapCache[F]] =
    for
      concreteSet <- AtomicCell[F].of(Map.empty[IoValue, ConcreteNodes[F]])
      abstractSet <- AtomicCell[F].of(Map.empty[HnId, AbstractMapNode[F]])
    yield new MapCache(mapGraph, concreteSet, abstractSet)
