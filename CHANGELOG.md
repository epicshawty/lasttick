# Changelog

All notable changes to this project are documented here. Format: [Keep a Changelog](https://keepachangelog.com/en/1.1.0/). Versioning: [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.1] - 2026-11-15

### Fixed

- The `Assist` keybind did nothing after being bound. 26.3's `KeyMapping.setKey` stores the new key without
  re-registering the mapping in its private key-to-mappings index, so a mapping created unbound never receives
  clicks or key state. The toggle now polls the bound key's raw scan code, and ignores presses while any screen
  is open so binding it in Controls does not fire it.

### Added

- `INFO` lines in `logs/latest.log` for each toggle and the first ten jumps, so state is checkable without
  in-game feedback.

## [1.0.0] - 2026-11-15

### Added

- Jump on the last grounded tick before the next tick's swept box stops being supported.
- `Assist` keybind under Controls → Miscellaneous. Off by default, unbound by default, no feedback of any kind.
- `config/lasttick.json` persisting the toggle across restarts.
