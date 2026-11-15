<div align="center">

<img src="https://raw.githubusercontent.com/epicshawty/lasttick/main/docs/banner.svg" alt="LastTick" width="860">

A vibe-coded, AI-generated reimplementation of the parkour macro Dream used in MCC.

<br>

<a href="https://fabricmc.net/develop"><img src="https://img.shields.io/badge/Minecraft-26.3-62b47a?style=flat-square&logo=minecraft&logoColor=white" alt="Minecraft 26.3"></a>
<a href="https://fabricmc.net/develop"><img src="https://img.shields.io/badge/Fabric_Loader-0.20.1-d0c7bd?style=flat-square" alt="Fabric Loader 0.20.1"></a>
<a href="https://adoptium.net"><img src="https://img.shields.io/badge/Java-25-ed8b00?style=flat-square&logo=openjdk&logoColor=white" alt="Java 25"></a>

<a href="https://github.com/epicshawty/lasttick/releases/latest"><img src="https://img.shields.io/github/v/release/epicshawty/lasttick?style=flat-square&label=release&color=f25202" alt="Latest release"></a>
<a href="https://github.com/epicshawty/lasttick/releases"><img src="https://img.shields.io/github/downloads/epicshawty/lasttick/total?style=flat-square&label=downloads&color=ff7e08" alt="Downloads"></a>
<a href="LICENSE"><img src="https://img.shields.io/github/license/epicshawty/lasttick?style=flat-square&label=license" alt="AGPL-3.0-or-later"></a>

<a href="#"><img src="https://img.shields.io/badge/tests-0-lightgrey?style=flat-square" alt="Zero tests"></a>
<a href="#"><img src="https://img.shields.io/badge/vibes-extensive-brightgreen?style=flat-square" alt="Vibes extensive"></a>
<a href="#"><img src="https://img.shields.io/badge/mixins-none-4c566a?style=flat-square" alt="No mixins"></a>

</div>

---

> **This is a macro, not a mod.** It decides when you jump, using information your eyes were supposed to
> process manually. Servers call that cheating, anticheats call it a fun afternoon, and the subreddit calls
> it a 47-minute video essay. Single player, your own realm, or a private server where everyone else is
> running it too.

## The origin story, in which nobody learns anything

In 2020 Dream was accused of running an auto-jump macro during MCC parkour. The evidence was eleven frames of
video and a collective mood. The defence was a Google Doc. The verdict was a discourse that outlived several
hard drives. Nobody ever saw the jar.

Now there is a jar. It is roughly 150 lines of Java, produced by an AI that was asked "make it jump on the
last possible tick" and then left unsupervised in a Gradle project for one afternoon. No config screen, no
particles, no advancements, no lore, no moderation dashboard, no apology. It does one dishonest thing and
then waits.

Every other README in this category would now claim the code is battle-tested and used by thousands. This one
has a [Status](#status-confirmed-working-but-read-the-table) section instead, and the table in it is not
flattering.

## How the cheating works, mechanically

Every client tick, before vanilla processes your keyboard:

| Step | What happens |
|:---|:---|
| Predict | Take today's position and add `deltaMovement` — that is literally the box vanilla moves you to next tick |
| Probe | Drop that box 1/16 of a block and ask the level for real block collisions |
| Decide | Nothing under it, and this tick still has floor → jump now |

No look-ahead samples, no tuned scan distance, no "aggression" slider to pretend the thing is configurable.
One vector addition and one collision query. That is somehow the entire mod.

## Why it's the *last* tick and not the second-to-last

`jumpFromGround` requires `onGround`. One tick after the prediction fails you are airborne, and the game will
not let you jump no matter how sincerely you mash space. So the final tick with a block under you is not "a
good time to jump" — it is the only remaining time a jump exists. That is physics, not taste, and it is the
one part of this repository that is not slop.

## Things that stop it

| Gate | Why it's there |
|:---|:---|
| `onGround` | Can't jump mid-air. Tried. |
| Speed ≥ 0.05 blocks/tick | Standing at an edge is not "walking off" it |
| Sneaking / crouching | Vanilla already stops you at edges; jumping would be a betrayal |
| Water, lava, ladders, vines | Different physics, different mod, different cheat |
| Spectator, passenger, elytra | Not a walk |
| `mayfly` / flying | Creative players have flight; a mod that yanks you off the ground there is a hazard |
| Jump key already held | You are already jumping. It can tell. |

Any ledge counts, including a six-block drop, including a single-block step down, because the block does in
fact end there. If that annoys you, that is a philosophical disagreement with the premise, not a bug.

## The toggle nobody can see

`Controls → Miscellaneous → Assist`. Unbound by default. No chat message, no toast, no sound, no particle, no
boss bar, no hotbar flash, no Discord webhook. The entire feedback budget of this software is a line in
`logs/latest.log`:

```
lasttick: enabled=true (key event)
```

Two other ways to tell, both requiring you to leave the game: `config/lasttick.json`, and the apoplectic tone
of the person who just watched you clear a gap you had no business clearing.

While armed it also writes one `lasttick: probe …` line per second of ground movement, listing every gate and
the distance along your heading to the first unsupported position. That is not a feature. That is a diagnostic
that shipped because two releases in a row were broken in ways this exact line would have explained in ten
seconds.

## Status: confirmed working, but read the table

| Claim | Reality |
|:---|:---|
| Jumps on the last tick | Verified offline on a synthetic ledge (fires on the tick the box is still on the floor, not the one after), and confirmed in a live survival world |
| Works in creative | No, deliberately — `mayfly` is a gate |
| The keybind reads your mind | It reads `consumeClick()`. That is not the same thing |
| Zero bugs ever shipped | 1.0.0 shipped with the predicate unreachable, 1.0.1 shipped a "fix" that toggled twice per press and cancelled itself out, 1.0.2 is the one that works |
| Tested | Zero test files in the repo. The offline geometry harness lives outside it, because committing it would have required a decision |

If it ever stops working, `logs/latest.log` names the gate that refused: bind the key, press it once, look for
`lasttick: bound=… enabled=true`, then walk with it armed and read the `probe` lines. This is more logging than
the mod deserves and less than it needs.

## Requirements

- Minecraft 26.3, Fabric Loader ≥ 0.16.0
- Fabric API
- Java 25
- A server that does not read the logs

## Install

```bash
cp lasttick-1.0.2.jar ~/.minecraft/mods/
```

Bind `Assist`, press it once, walk off something. There is no HUD, so "did it turn on" is a leap of faith
followed immediately by a fall.

## Versions

Semantic versioning, one tag per release, jar attached to the tag by GitHub Actions, because a human cannot
be trusted to remember `./gradlew build` before uploading.

| Bump | Meaning here |
|:---|:---|
| major | config path or mod id changes, or Minecraft does |
| minor | new gate, or a behaviour change you can feel |
| patch | a fix that doesn't fix anything you asked for |

## License

AGPL-3.0-or-later — the most copyleft thing available without involving a lawyer. Full text in
[LICENSE](LICENSE).

Not affiliated with Dream, MCC, Mojang, Microsoft, or any anticheat vendor. No Mojang code is redistributed
here. The premise is a joke; the collision maths is not.
