**Documentation**: 
* [Learning Akka Typed from Classic](https://doc.akka.io/libraries/akka-core/current/typed/from-classic.html)
* [Introduction to Actors](https://doc.akka.io/libraries/akka-core/current/typed/actors.html)
* [#Integrating Akka with Cats-Effect 3](https://alexn.org/blog/2023/04/17/integrating-akka-with-cats-effect-3/)

## Notes
---

**SBT commands:**
* `sbt compile`
* `sbt scalafmt`


## Prompts 

---

#### Actor visualization algo:
Implement visualization algo: 
1. `ManagerActor` after nodes added should send `Structure.Nodes.Added` message to `VisualizerActor`
2.  `ManagerActor` after edges added should send  `Structure.Edges.Added` message to `VisualizerActor`
3. `Structure.Nodes.Added` and  `Structure.Edges.Added` should be defined in  `planning.engine.planner.mpi.actors.visualizer.Messages`
4. `Structure.Nodes.Added` should contain `ids: Map[MnId, Option[HnName]]`
5. `Structure.Edges.Added` should contain `keys: Set[MeKey]`
6.  `VisualizerActor` should receive  `Structure.Nodes.Added` and `Structure.Edges.Added`and save added nodes and edges in it's state `planning.engine.planner.mpi.actors.visualizer.data.States.State`
7. `VisualizerActor` should have field `conNodes: Map[MnId.Con, Option[HnName]]`, `absNodes: Map[MnId.Abs, Option[HnName]]`, `srcLinkMap: Map[MnId, Set[Link.End]]`, `srcThenMap: Map[MnId, Set[Then.End]]`, `trgLinkMap: Map[MnId, Set[Link.End]]`,  `trgThenMap: Map[MnId, Set[Then.End]]`

Replace all akka `!` operators with `planning.engine.planner.mpi.common.actor.ActorRefEx.send` helper method

In `planning.engine.planner.mpi.actors.visualizer.data.States.State` remove validation that check if edge or node already added. Just replace with new if it already exist.

In `ManageEdgesSpec` remove `"notify VisualizerActor with the upserted edges"` test. Instead add validation of `VisualizerActor.Structure.Edges.Added` sent in `"upsert a single edge"` and in `"upsert multiple edges from a single UpsertEdges message"`

Write tests for  `planning.engine.planner.mpi.actors.visualizer.data.States.State`

Update `NodeDataSpec` regard changed implementation 




## Sticky notes 

---

```
def traverse_[G[_], B](f: A => G[B])(implicit G: Applicative[G]): G[Unit] =  traverseVoid[G, B](f)
def  traverse[G[_], B](f: A => G[B])(implicit ev$1: Applicative[G]): G[F[B]] = typeClassInstance.traverse[G, A, B](self)(f)


"During processing message:\n"










```





