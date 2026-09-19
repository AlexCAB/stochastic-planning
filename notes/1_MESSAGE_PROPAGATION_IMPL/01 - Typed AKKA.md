**Documentation**: 
* [Learning Akka Typed from Classic](https://doc.akka.io/libraries/akka-core/current/typed/from-classic.html)
* [Introduction to Actors](https://doc.akka.io/libraries/akka-core/current/typed/actors.html)
* [#Integrating Akka with Cats-Effect 3](https://alexn.org/blog/2023/04/17/integrating-akka-with-cats-effect-3/)

## Notes
---

**SBT commands:**
* `sbt compile`
* `sbt scalafmtAll`


## Prompts 

---

#### Actor visualization algo:
❯ Implement visualization algo: 
1. `ManagerActor` after nodes added should send `Structure.Nodes.Added` message to `VisualizerActor`
2.  `ManagerActor` after edges added should send  `Structure.Edges.Added` message to `VisualizerActor`
3. `Structure.Nodes.Added` and  `Structure.Edges.Added` should be defined in  `planning.engine.planner.mpi.actors.visualizer.Messages`
4. `Structure.Nodes.Added` should contain `ids: Map[MnId, Option[HnName]]`
5. `Structure.Edges.Added` should contain `keys: Set[MeKey]`
6.  `VisualizerActor` should receive  `Structure.Nodes.Added` and `Structure.Edges.Added`and save added nodes and edges in it's state `planning.engine.planner.mpi.actors.visualizer.data.States.State`
7. `VisualizerActor` should have field `conNodes: Map[MnId.Con, Option[HnName]]`, `absNodes: Map[MnId.Abs, Option[HnName]]`, `srcLinkMap: Map[MnId, Set[Link.End]]`, `srcThenMap: Map[MnId, Set[Then.End]]`, `trgLinkMap: Map[MnId, Set[Link.End]]`,  `trgThenMap: Map[MnId, Set[Then.End]]`

❯ Replace all akka `!` operators with `planning.engine.planner.mpi.common.actor.ActorRefEx.send` helper method

❯ In `planning.engine.planner.mpi.actors.visualizer.data.States.State` remove validation that check if edge or node already added. Just replace with new if it already exist.

❯ In `ManageEdgesSpec` remove `"notify VisualizerActor with the upserted edges"` test. Instead add validation of `VisualizerActor.Structure.Edges.Added` sent in `"upsert a single edge"` and in `"upsert multiple edges from a single UpsertEdges message"`

❯ Write tests for  `planning.engine.planner.mpi.actors.visualizer.data.States.State`

❯ Update `NodeDataSpec` regard changed implementation 

❯ Analise previous git commit and refactor `planning/engine/planner/mpi/actors/node` and `planning/engine/planner/mpi/actors/visualizer` actor so they will have same structure as `planning/engine/planner/mpi/actors/manager`. Do not fix compilation errors. 

❯ Fix compilation errors in implementation (but not in tests). Ask about each error before fix.

❯ Replace `ActorRefEx.send` with the call of actor API method form `planning/engine/planner/mpi/actors/manager/Manager.scala` or `planning/engine/planner/mpi/actors/node/Node.scala` or `planning/engine/planner/mpi/actors/visualizer/Visualizer.scala`

❯ Remove `StaticActors. apply()`, instead create `StaticActors` where it was called

❯ In the `object Node` add `type Msg = Actor.Msg` to make `Actor.Msg` publicly available. Implement `Node.spawn` so it will construct `Actor.Def` inside. 

❯ Implement method `protected def askF[F[_]: MonadThrow, R](msg: M): F[R]`

❯ Refactor and fix unit tests regard refactored implementation

❯ Similar to `planning.engine.planner.mpi.actors.manager.FakeManager` also add `FakeNode` and `FakeVisualizer`

❯ Refactor test `ManagerActor.doHandleManagerError` in `HandleErrorSpec` similar to `ManagerActor.doHandleNodeError`

❯ Refactor tests in `ManageEdgesSpec` similar to `HandleErrorSpec`

❯ Refactor tests in `ManageNodesSpec` similar to `ManageEdgesSpec`

❯ In `ManageNodesSpec` and `ManageEdgesSpec` for each `fakeVisualizer.probe.expectMessageType[Visualizer.Msg]` add result verification 

❯ Replace rest `verifyVisualizerNotified` with patter like `fakeVisualizer.expectShowNodesAdded mustBe conRes`

❯ Refactor tests in `NodeStructureSpec` similar to `ManageEdgesSpec`

❯ Similar to  `FakeVisualizer` move all `*.probe.expectMessage*` to `FakeNode`

❯ Refactor tests in `NodeStructureSpec` similar to `ManageEdgesSpec`

❯ Implement `VisualizerStructureSpec` test for `Visualizer`, similar to how it done for Node in `NodeStructureSpec` tests

❯ Analise compilation error, suggest how to fix

❯ Add `getState` method to `FakeNode` and `FakeVisualizer` as it done in `FakeManager`

❯ In test `"ManagerActorSpec.AddNodes"` after `val state = managerEmpty.manager.getState` add state validation

❯ Also in tests `ManagerNodesSpec`add state validation at the end of "upsert node by name" test

❯ Now  in tests `ManagerNodesSpec` move state validation in helpers function, so only expected values and manager will be passed to it.

❯ In `"ManagerActor.UpsertEdges"` add  state validation, similar to `ManagerNodesSpec` tests

❯ In `NodeStructureSpec` in `"add source end of the edge` and `"add target end of the edge"` tests add  state validation, similar to `ManagerNodesSpec` tests. 

❯ In `VisualizerStructureSpec` tests add  state validation, similar to `ManagerNodesSpec` tests. 

❯ Consider new API definition  in `planning.engine.planner.mpi.actors.manager.Manager` trait. Update `planning.engine.planner.mpi.actors.manager.logic.ApiImpl` and  `planning.engine.planner.mpi.actors.manager.data.Message` regard new  new API definition. Do not compile.

❯ Consider new messages in `planning.engine.planner.mpi.actors.manager.data.Message`. In `planning.engine.planner.mpi.actors.manager.logic` add new handlers of this messages without implementation (use ??? operator) . Do not remove old handlers and do not compile.

❯ Consider new messages in `planning.engine.planner.mpi.actors.manager.data.Message`. In `planning.engine.planner.mpi.actors.manager.logic.Actor` update  `receive` function. Do not compile.

❯ Refactor `FakeNode` and `NodeStateSpec` regard updated `planning.engine.planner.mpi.actors.node.data.State`

❯ Refactor `NodeStateSpec` regard updated `planning.engine.planner.mpi.actors.node.data.State`

❯ Refactor rest tests in `NodeStateSpec` so they will be similar to `"add edge to outgoing map and sample map when empty"` test

❯ Refactor tests in `NodeStateSpec` so they will be similar to `"add edge to outgoing map and sample map when empty"` test

❯ Write tests in `NodeStateSpec` for new implementation of `planning.engine.planner.mpi.actors.node.data.State`, without live actor.

❯ To implement `doAddManSamples` method, it should:
1. Validate if all `MnId` used in `msg.samples` are in `msg.nodes` also.
2. With `upsertNodesByName` to create of find nodes from `msg.samples` and get they `MnId`'s.
3. With `manager.data.State.withNewSamples` to add samples from `msg.samples` to manager state and get they `SampleId`'s.
4. For each `MeKey` in each `Sample.Man.edges` for each sample in `msg.samples` to replace `Nim` with the corresponding `MnId` got from `upsertNodesByName`.
5. Take all `MeKey` from each `Sample.Man.edges` in `msg.samples` an group them by `MeKey`, so result collection will nave type `Map[MeKey, Set[SampleId]]`.
6. For each element from collection above to add edge using `upsertEdge` method. 
7. Report to the sender with `ManSamplesAdded` message. 

❯ Refactor and add new tests in `ManagerStateSpec` regard new implementation if `planning.engine.planner.mpi.actors.manager.data.State`.

❯ Refactor tests in `ManagerStateSpec`, without using `UnitSpecWithIOAndTestKit`. Consider as example `NodeStateSpec`.

❯ In `ManagerStateSpec` refactor test so they will use `async[IO]:`/`await`. Tests to refactor:
1. `add a named node to nodeRefMap and nodeNameMap, and increment nextMnId`
2. `add nodes with duplicate data`
3. `add manual samples to sampleDataMap and increment nextSampleId`
4. `add generated samples to sampleDataMap with no info and increment nextSampleId`
5. `return sample data for known sample IDs`

❯ Also add in `claude.md` rule that complex tests (which have more then one matcher) have to be written with using  `async[IO]:`/`await`.

❯ Implement `"NodeData.apply(Option[IoValue])"` test for `planning.engine.planner.mpi.common.data.node.NodeData.apply(ioValue: Option[IoValue]): NodeData` method.

❯ In `ManagerNodesSpec` refactor tests `"Manager.addNode(...)"`  and `"Manager.upsertNodesByName(...)"` regard new definitions in `planning.engine.planner.mpi.actors.manager.Manager` and implementation in `planning.engine.planner.mpi.actors.manager.logic.Nodes`

❯ In `ManagerEdgesSpec` refactor tests `"Manager.addEdge(...)"` regard new definitions in `planning.engine.planner.mpi.actors.manager.Manager` and implementation in `planning.engine.planner.mpi.actors.manager.logic.Edges`

❯ In `ManagerSamplesSpec` write tests for methods `addManSamples` and `addGenSamples` of `planning.engine.planner.mpi.actors.manager.Manager`. Find implementation of this methods in `planning.engine.planner.mpi.actors.manager.logic.Samples`.

❯ Fix `ManagerStateSpec` and `ManagerSamplesSpec` tests regard new implementation. Also in `ManagerStateSpec` add test for `getNodes` method.

❯ Add configuration in `.scalafmt.conf` to allow in line `if` like `)(using d: Def, ctx: Ctx): F[Set[MeKey]] = if sampleMap.isEmpty then Set.empty.pure else`

❯ In `planning.engine.planner.mpi.actors` add new actor with name `planner` and structure similar to `planning.engine.planner.mpi.actors.visualizer, it should:
1. API should have first method: `def step(observation: Observation): F[Action]`. Add it definition in `Planner` send message implementation in `ApiImpl`, messages in `object Message`, and handler in `Actor`. Future implementation of `step` method should be placed in `SimpleSyncPlanner` trait.
2. API should have second method: `def conNodeAdded(node: Node): F[Unit]`
3. `Definition` should have 2 fields: `inputVariables: Map[IoName, InputNode[F]]` and `outputVariables: Map[IoName, OutputNode[F]]`
4. `State` should have 2 fields: `inputNodes: Map[IoName, Map[HnIndex, Set[Node]]]` and `outputNodes: Map[MnId.Con, (HnIndex, Node)]` 

❯ Configure `.scalafix.conf` to fold import statements, for example: `import planning.engine.common.values.text.{Description, Name}` instead of:
```
import planning.engine.common.values.text.Description  
import planning.engine.common.values.text.Name
```

❯ Also add `FakePlanner`, `TestPlanner` and `WithTestPlanner`.


❯ Draw actors parent-child graph in pseudo-graphic: On top is `root` actor. Next level connected to `root` is `manager` and `visualizer` and `planner`. Bottom level is set of `node` actors, connected to `manager`.

❯ Draw actors dependency graph in pseudo-graphic: On top is `manager` actor. Next level connected to `manager` is `visualizer` and `planner`. Bottom level is `node` actor, connected to `manager`, `planner` and `visualizer`. 
Add arrows: `planner` and `visualizer` to `manager`, `planner` and `visualizer` and  `manager` to `node`. 

❯ Run unit tests in `pe-planner-mpi` and fix that failed.

❯ In `actors.planner.data.State` implement:
1. `withNewInNodes` which add data to `inputNodes` collection
2. `withNewOutNodes` which add data to `outputNodes` collection

❯ In `actors.planner.data.State` rewrite `withNewOutNodes` similarly to `withNewInNodes`.

❯ Implement `PlannerSateSpec` for `actors.planner.data.State`

❯ Implement `PlannerDefinitionSpec` for `actors.planner.data.Definition`

❯ Implement `PlannerDefinitionSpec` add success case for `conNodesByType` 

❯ Implement `PlannerStructureSpec` for `actors.planner.logic.Structure`, similarly as it done in `VisualizerStructureSpec`

❯ Implement VariableSpec for `mpi.common.io.Variable`, for now only for `validateNode` method

❯ Implement TypeSpec for `mpi.common.io.Type`, for all types: `N`, `R`, `Bool`, `Opt`

❯ In `scala/planning/engine/planner/mpi/actors/guardian/logic/ApiImpl.scala` add implementation similar to `scala/planning/engine/planner/mpi/actors/manager/logic/ApiImpl.scala`

❯ In `scala/planning/engine/planner/mpi/actors/guardian` add `TestGuardian` and `WithTestGuardian` classes, similar as it done in `TestVisualizer` and `WithTestVisualizer` (except state related methods, since Guardian do not have state).

❯ Implement `GuardianLifecycleSpec` tests using `WithTestGuardian`, similarly to `VisualizerStructureSpec`. It should have:
1. Test for `Guardian.initialize(...)`: Check that for not initialized method return created actors. And for initialized Guardian actor terminate. 
2. Test for `Guardian.reset(...)`: Check it passes ok for initialized and not initialized. 

❯ Refactor `GuardianLifecycleSpec` instead of define new `lazy val visualization: Visualization = new Visualization` use the one defined in `WithTestVisualizer`


❯ Implement `MapMpiImplSpec`:
1. Create and use `stub`'s for `Guardian`, `Manager`, `Planner` and `Visualization`.
2. Add test for `MapMpiImpl.init(...)`: Check if `Guardian.initialize` called with proper params. 
3. Add test for `MapMpiImpl.reset(...)`: Check if `Guardian.reset` called. 
4. Add test for `MapMpiImpl.addSamples(...)`: Check if `Manager.addManSamples` called. 

❯ Refactor `MapMpiImplSpec`: Use `scalamock` `stub` like `val plannerStub: Planner = stub[Planner]` for `Guardian`, `Manager`. instead of define it as separate class.

❯ Refactor `MapMpiImplSpec`: 
1. Replace mocking library form `scalamock` to Scala `mockito` and refactor related code.
2. Use Mockito `mock` like `val plannerStub = mock[Planner]` for `Guardian`, `Manager`. instead of define it as separate class.

❯ Add to `claude.md`: do not mix `async[IO]:` and `.asserting: result =>` in test. 
Bad example:
```
async[IO]:  
  mapMpi.init(vars).logValue(tn).await  
  mapMpi.addSamples(samples, nodes).logValue(tn).await  
.asserting: result =>  
  managerStub.addManSamples[IO](samples, nodes) was called  
  result mustBe Map.empty
```
Good example:
```
async[IO]:  
  mapMpi.init(vars).logValue(tn).await  
  val result = mapMpi.addSamples(samples, nodes).logValue(tn).await  
  
  managerStub.addManSamples[IO](samples, nodes) was called  
  result mustBe Map.empty
```

Use `.asserting: result =>` only for on matcher tests.

❯ Investigate warnings, try to fix:
```
Mockito is currently self-attaching to enable the inline-mock-maker. This will no longer work in future releases of the JDK. Please add Mockito as an agent to your build as described in Mockito's documentation: https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html#0.3  
WARNING: A Java agent has been loaded dynamically (C:\Users\cabem\AppData\Local\Coursier\Cache\v1\https\repo1.maven.org\maven2\net\bytebuddy\byte-buddy-agent\1.17.7\byte-buddy-agent-1.17.7.jar)  
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning  
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information  
WARNING: Dynamic loading of agents will be disallowed by default in a future release
```

❯ Run via all tests and refactor the tests which use `scalamock` so they will use Mockito.




##### TODO: 
1. Integration with REST API (test with loading script)
2. Implement graph representation (using colored text)
3. Implement basic visualization API and integrate with Python





## Sticky notes 

---

```
def traverse_[G[_], B](f: A => G[B])(implicit G: Applicative[G]): G[Unit] =  traverseVoid[G, B](f)
def  traverse[G[_], B](f: A => G[B])(implicit ev$1: Applicative[G]): G[F[B]] = typeClassInstance.traverse[G, A, B](self)(f)


"During processing message:\n"



unorderedTraverse



sbt "planner_mpi/testOnly *MapMpiImplSpec"



def withNewNodes[F[_]: MonadThrow](  
    data: NodeData.Kit,  
    spawn: Map[MnId, NodeData] => Set[Node],  
): F[(Map[Node, Definition], State)] =  
  def extractNames(newNodes: Map[Node, NodeDef]): Map[HnName, Set[MnId]] = newNodes  
    .values.collect { case d if d.data.name.isDefined => d.data.name.get -> d.id }  
    .groupBy(_._1).map((name, ids) => name -> (ids.map(_._2).toSet ++ nodeNameMap.getOrElse(name, Set.empty)))  
  
  def updateState(newNodes: Map[Node, NodeDef]): State = this.copy(  
    nodeRefMap = nodeRefMap ++ newNodes.map((r, d) => d.id -> r),  
    nodeNameMap = nodeNameMap ++ extractNames(newNodes),  
    nextId = nextId + newNodes.size,  
  )  
  
  for  
    definitions <- data.nodes.zipWithIndex.traverse((node, i) => node.toDefinition(nextId + i, actors))  
    msIds = definitions.map(_.id)  
    _ <- msIds.assertDistinct("Duplicate node IDs in new nodes")  
    _ <- nodeRefMap.values.assertContainsNoneOf(msIds, "Node IDs already exist in the current state")  
    nodeRefs = spawn(definitions)  
  yield (nodeRefs, updateState(nodeRefs))




AbsData

NodeData.Abs



// Mockito's inline mock maker self-attaches as a Java agent at runtime  
Test / fork := true  
Test / javaOptions += "-Xshare:off"  
Test / javaOptions ++= (Test / dependencyClasspath).value  
  .map(a => fileConverter.value.toPath(a.data))  
  .find(_.getFileName.toString.startsWith("mockito-core-"))  
  .map(p => s"-javaagent:${p.toAbsolutePath}")  
  .toSeq


Params for Idea ScalaTest runnner:
  --sun-misc-unsafe-memory-access=allow
  -javaagent:C:\Users\cabem\AppData\Local\Coursier\Cache\v1\https\repo1.maven.org\maven2\org\mockito\mockito-core\5.23.0\mockito-core-5.23.0.jar
  -Xshare:off


```












