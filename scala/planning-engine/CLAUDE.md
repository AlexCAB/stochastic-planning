# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Scala 3 / sbt multi-module research codebase for stochastic planning: a probabilistic map + planning-DAG engine backed by Neo4j, exposed over an http4s REST API. Two independent planner engines exist side by side (GSI and MPI — see Architecture).

## Commands

- Build everything: `sbt compile`
- Run routine tests (all modules except `itAndTools`): `sbt "common/test" "map/test" "planner_gsi/test" "planner_mpi/test" "api/test"`
- Run one module's tests: `sbt "planner_mpi/test"`
- Run a single spec: `sbt "planner_mpi/testOnly *HandleErrorSpec"` (or the fully-qualified name)
- Format: `sbt scalafmtAll` (check without changing: `sbt scalafmtCheckAll`). scalafmt is configured with `rewrite.scala3.removeOptionalBraces = yes`, so it auto-rewrites brace blocks to colon/indentation syntax — run it after generating code instead of hand-fixing brace style.
- Lint/organize imports: `sbt scalafixAll` (rules in `.scalafix.conf`: RemoveUnused, OrganizeImports, DisableSyntax, LeakingImplicitClassVal, ProcedureSyntax, RedundantSyntax)
- sbt project key per module directory (from root `build.sbt`): `pe-common`→`common`, `pe-map`→`map`, `pe-planner-gsi`→`planner_gsi`, `pe-planner-mpi`→`planner_mpi`, `pe-rest-api`→`api`, `pe-tools-and-it`→`itAndTools`.
- Do not run tests in `pe-tools-and-it` (`itAndTools`). They are manual/integration tests meant to run against a live Neo4j instance, not part of routine/automated runs.
- Code that touches the DB (`pe-map`, `pe-tools-and-it`) expects a local Neo4j at `neo4j://localhost:7687` — default creds are in `pe-common/src/main/resources/db.conf`.

## Architecture

Module dependency direction (root `build.sbt`): `common` ← `map` ← {`planner_gsi`, `planner_mpi`} ← `api` ← `itAndTools`. Every module compile/test-depends on `common`.

- **`pe-common`** — shared kernel, no dependencies on any other module.
  - `values/node`: the three-tier node-ID hierarchy used across every module — `HnId` (Hidden Node ID, the DB/graph-persisted identity) → `MnId` (`Con`/`Abs` — "Concrete"/"Abstract" map-node ID, stronger-typed than `HnId` for planner-internal use) → `PnId` (Plan Node ID, wraps an `MnId` plus a usage `count` for the planning DAG). Conversions between tiers (`HnId.toMnId`, `MnId.asHnId`, ...) are explicit and validated, never assumed.
  - `graph`: generic graph algorithms (edges/paths/trees), keyed by `MeKey` (`Link`/`Then`, mirroring `EdgeType.LINK`/`THEN`), shared by both planner engines.
  - `errors`: a validation DSL of `assertX` extension methods (`assertDistinct`, `assertOneElement`, `assertContainsNoneOf`, `assertEqual`, ...) that return `F[Unit]` and raise `AssertionError` via `ApplicativeThrow`/`MonadThrow`. This is the idiomatic way invariants are checked throughout the codebase — prefer it over ad-hoc `if/throw`.
- **`pe-map`** — Neo4j-backed persistence for the hidden-node graph (via neotypes).
- **`pe-planner-gsi`** ("Global State" implementation) and **`pe-planner-mpi`** ("Message Propagation" implementation) are two independent, parallel implementations of the same planning engine — don't assume concepts transfer between them beyond the shared `pe-common` model.
  - `planner-gsi` computes over an in-memory, immutable graph snapshot (`DcGraph`/`DagNode`); synchronous, no actors.
  - `planner-mpi` distributes state across live Pekko actors that message-pass to converge (see Actor pattern below).
- **`pe-rest-api`** — http4s + circe HTTP layer exposing both planners. `IOApp` entrypoints extend `AppBase` (builds the Ember server + routes); `MaintenanceService` handles health/shutdown.
- **`pe-tools-and-it`** — manual scratch/integration tooling against a live Neo4j; excluded from routine test runs (see Commands).

### Actor pattern (pe-planner-mpi)

Every actor (`ManagerActor`, `NodeActor`, `VisualizerActor`) is a singleton `object extends ActorBase`, assembled from small per-concern traits rather than one large class:
- `data.Definitions` / `data.States` — the actor's immutable `Def` (constructor args) and `St` (state, updated via `.copy`) types.
- `Messages` — the sealed `Message` hierarchy for that actor (extends `MessageRepr`/`ReplyTo[...]` for pprint-based logging and ask-style replies).
- `logic.*` traits (e.g. `ManageNodes`, `ManageEdges`, `HandleError`), each `self: XxxActor.type =>`, holding the actual `doXxx` handler implementations — one trait per group of related message handlers.

The object's `receive`/`error` just pattern-match on `Msg` and delegate to the `doXxx` methods. Any error escaping a handler is funneled through `HandleError`, which logs (rendering the message and state via `pprint`) and always raises `FatalException`; `ActorBase.behavior` treats that as unrecoverable and stops the actor — there's no retry/self-healing.

`ManagerActor` is the parent of all `NodeActor` instances (one per map node) and is reached from outside only through `ManagerAdaptor` — an ask-pattern adaptor bridging cats-effect code and Pekko actors (not a plain `ActorRef`).

## Testing conventions

- Specs are ScalaTest `FixtureAnyWordSpecLike`/`FixtureAsyncWordSpecLike` written with Scala 3 colon syntax (`"X" should:` / `"y" in newCase[CaseData]: (log, data) => ...`), not brace/`in { }` style.
- Test fixtures go in a `private class CaseData extends Case with SomeTestDataTrait`, instantiated fresh per test via `newCase[CaseData]`. Reusable fixtures (test actors, sample domain data) live under each module's `test.actors`/`test.data` packages.
- Actor tests spawn the real actor via its module's `*TestActor` helper (e.g. `ManagerTestActor`) and assert on messages received by a `TestProbe`, or on actor termination via `probe.expectTerminated(ref)` — they don't call `private[pkg]` `doXxx` handler methods directly, since those need a live `ActorContext`.
- Effectful (non-actor) code is tested with cats-effect via `UnitSpecIO`/`UnitSpecWithIOAndTestKit`, using `.asserting(...)` / `.assertThrowsError[T](...)`.

## Scala style

- No braces for blocks: use Scala 3 colon-indentation syntax (`trait X:`, `object Y:`, `if ... then:`, etc.), not `{ }`.
- Trailing lambda arguments use colon syntax, not braces:
  ```scala
  // preferred
  items.traverse: (key, data) =>
    doSomething(key, data)

  // avoid
  items.traverse { (key, data) =>
    doSomething(key, data)
  }
  ```
- Don't use `case (a, b) =>` to destructure a tuple parameter when a plain `(a, b) =>` works — reserve `case` for actual pattern matching (guards, sealed-trait matches).
- When a method body is a chain of calls on one value, keep the receiver on the `def`/`=` line and indent each subsequent call on its own line starting with `.`:
  ```scala
  private def extractNames(newNodes: Map[NodeActor.Ref, NodeActor.Def]): Map[HnName, Set[MnId]] = newNodes
    .values.collect { case d if d.data.name.isDefined => d.data.name.get -> d.id }
    .groupBy(_._1).map((name, ids) => ...)
  ```
- Don't nest a `for` expression inside another `for`/`traverse`/lambda — extract the inner one into a locally-defined named function and pass/call that instead, so every `for` block stays flat. Example (`doUpsertEdges` in `Edges.scala`, inner `for` extracted into `sendAddEdgeSrc`):
  ```scala
  private[manager] def doUpsertEdges[F[_]: S](msg: UpsertEdges, state: St)(using d: Def, ctx: Ctx): F[St] =
    def sendAddEdgeSrc(key: MeKey, data: EdgeData): F[Unit] =
      for
        srcRef <- state.getRef(key.src)
        trgRef <- state.getRef(key.trg)
        _ = srcRef ! NodeActor.AddEdgeSrc(MeRef(key, srcRef, trgRef), data)
      yield ()

    for
      _ <- msg.data.edges.toList.traverse(sendAddEdgeSrc)
      _ <- msg.replay(ManagerAdaptor.EdgesUpserted(msg.data.edges.keySet))
    yield state
  ```
- As file header use:
```
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
| created: DD.MM.YYYY |||||||||||*/
```
Replace `DD.MM.YYYY` with the actual creation date.
