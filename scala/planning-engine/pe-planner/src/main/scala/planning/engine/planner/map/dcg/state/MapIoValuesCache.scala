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
| created: 2025-12-02 |||||||||||*/

package planning.engine.planner.map.dcg.state

import cats.{ApplicativeThrow, MonadThrow}
import cats.effect.kernel.Concurrent
import cats.effect.std.AtomicCell
import cats.syntax.all.*
import planning.engine.common.errors.assertNoSameElems
import planning.engine.common.values.node.{HnId, IoIndex}
import planning.engine.common.values.text.Name
import planning.engine.map.subgraph.ConcreteWithParentIds
import planning.engine.planner.map.dcg.nodes.ConcreteMapNode

class MapIoValuesCache[F[_]: MonadThrow](
    load: Map[Name, IoIndex] => F[List[ConcreteWithParentIds[F]]],
    state: AtomicCell[F, Map[(Name, IoIndex), Map[HnId, ConcreteMapNode[F]]]]
):
  def get(values: Map[Name, IoIndex]): F[Map[(Name, IoIndex), List[ConcreteMapNode[F]]]] = state.evalModify: st =>
    for
      (notCachedKeys, cachedKeys) <- ApplicativeThrow[F].pure(values.partition(st.contains))
      cached = cachedKeys.map(k => k -> st.get(k).toList.flatMap(_.values))
      concreteNodes <- load(notCachedKeys)
      loadedNodes <- concreteNodes.traverse(n => ConcreteMapNode.fromMapNode(n.node))
      loaded = loadedNodes.groupBy(n => (n.ioNode.name, n.valueIndex))
      _ <- (loaded.keys, cached.keys).assertNoSameElems("loaded keys overlap with cached keys")
      _ <- (loaded.keys, st.keys).assertNoSameElems("loaded keys overlap with existing cached keys")
    yield (st ++ loaded.map((k, nodes) => k -> nodes.map(n => n.id -> n).toMap), cached ++ loaded)

object MapIoValuesCache:
  def apply[F[_]: Concurrent](load: Map[Name, IoIndex] => F[List[ConcreteWithParentIds[F]]]): F[MapIoValuesCache[F]] =
    for
        state <- AtomicCell[F].of(Map.empty[(Name, IoIndex), Map[HnId, ConcreteMapNode[F]]])
    yield new MapIoValuesCache[F](load, state)
