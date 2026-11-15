<div align="center">

<img src="https://raw.githubusercontent.com/epicshawty/lasttick/main/docs/banner.svg" alt="LastTick" width="860">

The parkour macro Dream used in MCC, rebuilt by an AI and shipped in public.

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

> This is a cheat. Not a mod, not a utility, not "accessibility". It decides whether to jump inside the 50
> milliseconds a tick gives you, and it decides correctly every time. Take it onto a server that didn't agree
> to this and you will get banned, and I'm not writing you a comforting paragraph about it afterwards.

## Where this came from

MCC 11, 2020. Dream gets accused of running an auto jump macro during the parkour. The evidence was a
slowed-down clip and a screenshot with a red circle drawn on it. The defence was a Google Doc with more words
in it than this entire codebase. Half the subreddit wanted him drawn and quartered, the other half wanted him
elected president of Minecraft, and a genuinely alarming number of people are still arguing about it.

Six years on, the scoreboard reads: nobody proved it, nobody produced the jar, the argument is still going in
a subreddit thread that's older than some of the competitors. So I had a bot write the jar. One afternoon,
about 150 lines of Java, zero oversight.

There's no deeper point here. It's a cheat for a children's game that got generated because the discourse
annoyed me and because "make it jump on the last possible tick" is a stupidly funny thing to ask a language
model.

## What it does

Every client tick, before vanilla reads your keyboard:

| Step | What happens |
|:---|:---|
| Predict | Take where you are and add `deltaMovement`. That's the exact box vanilla is about to shove you into |
| Probe | Drop that box a sixteenth of a block and ask the level for real block collisions |
| Decide | Nothing under it, and you've still got floor this tick, so jump |

One vector addition and one collision query. There's no look-ahead sampling, no scan distance to tune, no
"aggression" slider, no config screen pretending this thing has opinions. If you came here for nuance, you're
on the wrong repository and possibly the wrong website.

## Why the last tick is the last tick

`jumpFromGround` needs `onGround`. One tick after the prediction fails you are in the air, and the game does
not care how hard you mash the spacebar. So the final tick with a block underneath you isn't a good time to
jump. It's the only tick where jumping is still a thing that exists. Anyone who has ever missed a jump by one
frame knows this in their teeth.

## Shit that stops it

| Gate | Why |
|:---|:---|
| `onGround` | You can't jump in the air. I checked. |
| Speed under 0.05 blocks/tick | Standing at the edge looking at the drop isn't walking off it |
| Sneaking or crouching | Vanilla already refuses to walk you off ledges there. Jumping would be a betrayal of trust |
| Water, lava, ladders, vines | Different physics, different mod, different flavour of cheating |
| Spectator, passenger, elytra | None of those are walking |
| `mayfly` and creative flight | Creative players can already fly. Yanking them off the ground would just be rude |
| Jump key already down | You're already jumping. It's not going to fight you about it |

Every ledge counts. Six block drop, one block step down, doesn't matter, the block ends either way. If that
bothers you then you have a philosophical problem with the premise and not with the code.

## The switch nobody can see

`Controls → Miscellaneous → Assist`. Unbound by default. No chat message. No toast, no sound, no particle, no
boss bar, no hotbar flash, no webhook to a Discord server where your friends can watch you get clipped.

That was your call. Also it's just good practice: a cheat that announces itself is a cheat that gets you
banned by Friday.

What you get instead is a line in `logs/latest.log`:

```
lasttick: enabled=true (key event)
```

Two other ways to check, both of which require leaving the game: `config/lasttick.json`, and the tone of
whoever just watched you clear a gap you had no business clearing.

While it's armed it also writes one `lasttick: probe ...` line per second of walking, listing every gate and
how far ahead the floor runs out. I didn't put those in for you. They're there because I broke the mod twice
in a row and guessing was getting expensive.

## Honest status, because the badges above are decoration

| Claim | Reality |
|:---|:---|
| Jumps on the last tick | Checked offline on a fake ledge, then checked in a real survival world. It does the thing |
| Works in creative | No, on purpose. `mayfly` is a gate |
| Zero bugs ever shipped | 1.0.0 shipped with the predicate unreachable. 1.0.1 shipped a fix that toggled twice per press and cancelled itself out. 1.0.2 is the one that works |
| Tested | No test files in here. The offline harness lives on my disk, because adding it to the repo would have required making a decision |

If it ever stops, `logs/latest.log` will tell you which gate refused. Bind the key, press it once, look for
`lasttick: bound=... enabled=true`, then walk around with it on and read the probe lines. That's the whole
support plan.

## FAQ

**Will this get me banned?** On any server with a brain or a plugin, yes. Singleplayer, a realm you own, or a
private server where the admin is running it too. That's the whole list.

**Does it work in creative?** No. It's in the table above. Read the table.

**Why is there no config screen?** There's one setting and it's a switch, and the switch is your keybind. Any
more UI than that would be lying to you about how much is going on in here.

## You need

- Minecraft 26.3, Fabric Loader 0.16.0 or newer
- Fabric API
- Java 25
- A server that doesn't read its own logs

## Install

```bash
cp lasttick-1.0.2.jar ~/.minecraft/mods/
```

Bind `Assist`, press it once, then walk off something. There's no HUD, so "did it turn on" is a leap of faith
followed immediately by a fall.

## Versions

Semver, one tag per release, jar attached to the tag by GitHub Actions, because I cannot be trusted to
remember `./gradlew build` before clicking upload.

| Bump | Meaning here |
|:---|:---|
| major | config path or mod id changes, or Minecraft does |
| minor | new gate, or something you can feel |
| patch | a fix for something you didn't ask me to break |

## License

AGPL-3.0-or-later, the most copyleft thing you can get without hiring a lawyer. Full text in [LICENSE](LICENSE).

Not affiliated with Dream, MCC, Mojang, Microsoft, or anybody who runs an anticheat. No Mojang code is
redistributed here. The premise is a joke, the collision maths isn't, and if you got banned that's between you
and your god.
