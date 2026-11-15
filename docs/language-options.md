# Language options — "last-tick" jump macro for Fabric 26.3

## Hard constraint

A Fabric mod is a **JVM jar** loaded by Fabric Loader 0.20.1. There is no plugin API for other runtimes.
So a language is only "possible" in one of two shapes:

1. **Compiles to JVM bytecode** — any JVM language ships as-is.
2. **Native code + a bridge** — still needs a Java/Kotlin loader jar for `fabric.mod.json`, entrypoint and
   keybind; the native part rides in the jar under `natives/<os>/` and is `System.load`ed (JNI) or linked
   through the Java 25 Foreign Function & Memory API.

## What this mod actually demands from the language

| Need | Verdict |
|---|---|
| One per-tick client hook | `ClientTickEvents.START_CLIENT_TICK` (Fabric API) |
| One keybind | `KeyBindingHelper` |
| Mixins | **none required** |
| Block collision queries | `level.getBlockCollisions(...)` |

The mixin-free design matters: Mixin needs a **Java annotation processor** to emit refmaps, and that is the
thing that normally makes non-Java JVM languages painful (Scala, Clojure and Groovy cannot run it on their own
sources). With zero mixins that tax disappears, and 26.3 has no Yarn mappings anyway (Loom 1.19+ builds
against Mojmap, no remap step), so no language here needs a refmap.

Net: **the language is close to free choice.** That is the honest finding, and it means the decision is about
maintenance and jar size, not feasibility.

## Viable options

| # | Language | Ships as | 26.3 status | Jar | Effort | Real risk |
|---|---|---|---|---|---|---|
| A | **Java 25** | bytecode | proven, official template | ~15 KB | 1× | none |
| B | **Kotlin 2.4** | bytecode + nested stdlib | proven (Spectre ships on 26.3) | ~1.7 MB | 1.1× | none |
| C | **Scala 3** | bytecode + scala3-library | unproven on 26.3 | ~9 MB | 1.6× | build wiring only; interop with Mojmap is plain Java calls |
| D | **Groovy 5** | bytecode + groovy | unproven | ~7 MB | 1.4× | dynamic dispatch unless `@CompileStatic` everywhere |
| E | **Clojure 1.12** | AOT classes + clojure.jar | unproven | ~4 MB | 2× | ~1 s startup cost, reflection on every Mojmap call unless hinted |
| F | **Rust** (`fabric2rust` 0.1.2) | DLL via JNI + Java shim | **has bundled 26.3 mappings** | ~15 KB jar + ~200 KB DLL per OS | 2.5× | **Windows x64 only** runtime; every MC patch can shift mapped members |
| G | **Rust / C / C++ / Zig / Nim via own JNI** | natives per OS + JNI glue | unproven | ~10 KB + ~50 KB per OS | 3× | you own the ABI, 3-4 CI matrix builds, no debugger story |
| H | same set via **Java 25 FFM (Panama)** | natives per OS, no JNI headers | FFM is final in 25 | same | 2.5× | still per-OS binaries; only worth it if the hot code is heavy |
| I | **Go** | cgo → JNI | not viable | — | — | Go runtime installs signal handlers + its own scheduler inside the JVM process; crashes are nondeterministic |
| J | **JRuby / Rhino / LuaJ / GraalPy / JS** | script inside a Java shell | works in principle | 4-80 MB | 2-3× | payload bigger than the whole feature; Jython is Python 2 only |

## Dead ends (checked, not viable)

| Language | Why not |
|---|---|
| Eta (Haskell→JVM) | project abandoned 2021 |
| Frege | abandoned; cannot read Java 25 class files |
| Ceylon | archived 2017 |
| Fantom | no Java 25 support, no Gradle path |
| Gosu | commercial, effectively dead |
| Xtend | Eclipse-only tooling, unmaintained |
| Kotlin/Native | no JVM bytecode, no Java interop |
| GHC JVM backend | removed 2015 |
| C# via IKVM | translates .NET assemblies to bytecode; adds a whole runtime, unsupported on 25 |
| Wasm via Chicory | pure-Java WASM host; a second toolchain to run 20 lines of Rust |
| Ballerina | JVM, but no Gradle/Loom path and unmaintained for MC |
| Python (`jython`) | Python 2 only, dead since 2015 |

## Toolchains installed on this machine

Java 25.0.3 (Corretto) · Gradle 9.7.1 · Kotlin (via Gradle, no standalone `kotlinc`) · Rust 1.x gnu ·
Zig 0.17.0 · Go · Node 22.
Absent: Scala, Clojure, Groovy, Ruby, GHC, Nim, Crystal, .NET, Erlang, Julia.

## Recommendation

**A. Java 25.** Reasons, in order:

1. The whole mod is ~150 lines: a tick predicate and a keybind. Every other option adds infrastructure that
   buys nothing on that surface.
2. Zero build risk — official Loom template, `javac` is the fastest oracle for a version whose APIs I cannot
   run.
3. Smallest jar by an order of magnitude (15 KB vs 1.7 MB+). For a client macro that someone toggles silently,
   a 2 MB jar carrying a language runtime is a bad trade.
4. Native options cost Windows portability, which for a client mod means "works for me, not for anyone else".

**B. Kotlin** if you want it to match Spectre's house style — same proven status on 26.3, +1.7 MB.
**F. Rust** is the only non-JVM route with real 26.3 support today, and it is genuinely interesting, but it is
Windows x64 only and every Minecraft patch can move the members it maps.
**C/D/E/F** if the point is novelty rather than the mod. Not one of them is faster at runtime; the predicate
runs once per tick on already-warm JIT code.

## Mechanic spec (identical in every language above)

Tick order: `START_CLIENT_TICK` runs before vanilla handles keybinds and moves the player.

```
if (!enabled) return
if (player is null || dead || spectator || !player.onGround()) return
if (flying || elytra || in water/lava || on ladder || sneaking) return
if (horizontal speed < 0.08 && no forward input) return

next  = pos + deltaMovement                    // vanilla moves by exactly this each tick
box   = player bounding box at next, moved down 0.06
if (level.getBlockCollisions(player, box) yields any block) return   // next tick is still supported

jump  = real key press: options.keyJump.setDown(true) this tick, release at END_CLIENT_TICK
```

Why this is "the last possible tick" and not a scan: the test is the next tick's *actual* swept box, not
sampled points ahead of the player (the sampled-scan approach used by other auto-jump mods trades exactness for
tunables). Jumping is only possible while `onGround()`, so the tick where the projected box loses support is
by definition the last one that can jump.

Driving `options.keyJump` instead of calling `jumpFromGround()` keeps vanilla semantics: sprint boost, jump
potion, and any mod that also manipulates jump input all keep working.

## Toggle surface

- Keybind in **Controls → Miscellaneous** (no custom category row), label `Assist`.
- No chat, toast, sound, action bar or HUD element. State persists in `config/lasttick.json`.
- Default: **off**, unbound.
