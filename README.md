<div align="center">

![LastTick](docs/banner.svg)

Jumps on the last grounded tick before the next tick loses its block.

<br>

<a href="https://fabricmc.net/develop"><img src="https://img.shields.io/badge/Minecraft-26.3-62b47a?style=flat-square&logo=minecraft&logoColor=white" alt="Minecraft 26.3"></a>
<a href="https://fabricmc.net/develop"><img src="https://img.shields.io/badge/Fabric_Loader-0.20.1-d0c7bd?style=flat-square" alt="Fabric Loader 0.20.1"></a>
<a href="https://fabricmc.net/develop"><img src="https://img.shields.io/badge/Fabric_API-0.166.2%2B26.3-d0c7bd?style=flat-square" alt="Fabric API 0.166.2+26.3"></a>
<a href="https://adoptium.net"><img src="https://img.shields.io/badge/Java-25-ed8b00?style=flat-square&logo=openjdk&logoColor=white" alt="Java 25"></a>

<a href="https://github.com/epicshawty/lasttick/releases/latest"><img src="https://img.shields.io/badge/release-1.0.2-f25202?style=flat-square" alt="Release 1.0.2"></a>
<a href="LICENSE"><img src="https://img.shields.io/badge/license-AGPL--3.0--or--later-blue?style=flat-square" alt="AGPL-3.0-or-later"></a>
<img src="https://img.shields.io/badge/mixins-none-4c566a?style=flat-square" alt="No mixins">
<img src="https://img.shields.io/badge/feedback-none-4c566a?style=flat-square" alt="No feedback">

</div>

---

## What it does

One thing. If the next tick would put your box over air, and this tick still has ground under it, it jumps.
That is the last tick a jump is possible at all — `jumpFromGround` needs `onGround`.

No look-ahead samples, no tunable scan distance: the test is the next tick's actual box, translated by
`deltaMovement`, checked against real block collisions.

> One tick later you are airborne. One tick earlier and you jump short.

---

## When it fires

| Gate | Value |
|:---|:---|
| `onGround` | required |
| Horizontal speed | ≥ 0.05 blocks/tick |
| Spectator, passenger, elytra | skipped |
| Water, lava, ladder, vine | skipped |
| Crouching / sneak held | skipped |
| Creative or flying (`mayfly`) | skipped |
| Jump key already held | skipped |

Any ledge counts, including a six-block drop. A single-block step down counts too — the block does end.

---

## Toggle

`Controls → Miscellaneous → Assist`.

| | |
|:---|:---|
| Default | off, no key assigned |
| Feedback | none — no chat, toast, sound, HUD or action bar |
| Persistence | `config/lasttick.json` |
| Side | client only |
| Ignored while | a screen is open |

Confirm state without feedback: `config/lasttick.json`, or `logs/latest.log` — `lasttick: enabled=true (key event)`
per toggle, `lasttick: probe …` once a second while walking with it on, and one `lasttick: jump at x, y, z`
per jump (first ten).

---

## Requirements

- Minecraft 26.3, Fabric Loader ≥ 0.16.0
- Fabric API
- Java 25

## Install

```bash
cp lasttick-1.0.2.jar ~/.minecraft/mods/
```

Then bind `Assist`, press it once, walk off something.

---

## Versions

Semantic versioning, tag per release, jar attached to the tag by GitHub Actions.

| Bump | Meaning here |
|:---|:---|
| major | config path or mod id changes, or a Minecraft jump |
| minor | new gate, behaviour change you can feel |
| patch | fix that changes no outcome you asked for |

## Layout

```
src/client/java/dev/epicshawty/lasttick/   EdgeJump, LastTickClient, Config
src/main/resources/                        fabric.mod.json, lang, icon
docs/banner.svg                            the header above
```

## License

AGPL-3.0-or-later. Full text in [LICENSE](LICENSE).
