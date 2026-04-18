# ZeroToler — Battle Physics & Arena Code Map

This document lists **all code responsible for**:

- Spin tops **movement** during battle
- Spin tops **collision with each other**
- Spin tops **collision with the arena**
- Arena **measurements** (radius + stabilization rings) and where they are drawn

It also includes the **exact file locations** in the project.

---

## Core battle simulation (authoritative physics + combat)

### `app/src/main/java/com/zerotoler/rpgmenu/domain/engine/BattleEngine.kt`

**What it is**

- The **runtime battle engine actually used by the game UI** (`RealBattleViewModel` creates this class).
- Uses **Dyn4j** for rigid-body integration and then layers Beyblade-like behavior on top:
  - ring/center stabilization
  - attack timer + dash + “magnetism”
  - arena boundary bounce
  - HP + stamina rules (defeat when HP == 0 or stamina == 0)

**Movement / physics responsibilities**

- **Update loop**: `fixedStep(dt: Float)`
  - Steps Dyn4j world
  - Applies stabilization + attack forces
  - Enforces arena bounds
  - Drains stamina and applies contact damage
- **Attack system**:
  - `manageAttacks(dt: Double)`
  - `applyAttackAndStabilization(self: Body, enemy: Body, dt: Double)`
- **Stabilization system (CENTER / INNER_RING / OUTER_RING)**:
  - `applyStabilizationForces(self: Body, rt: TopRuntime, dt: Double, mul: Double)`
  - Ring radii are defined relative to arena radius:
    - `innerRingRadius = arenaRadius * 0.37`
    - `outerRingRadius = arenaRadius * 0.74`
- **Arena collision / bounce**:
  - `enforceArenaBounce(body: Body, radius: Double)`
- **Top-top collision damage & interruption**:
  - `applyHpDamageIfContact()` does the contact test, interrupts attacks, and applies HP/stamina effects.

**Arena measurements (simulation space)**

- `arenaRadius` (currently set to `1.0` in this engine)
- Stabilization grooves (relative to `arenaRadius`):
  - inner: `0.37 * arenaRadius`
  - outer: `0.74 * arenaRadius`

---

## Top build → stabilization mapping (driver/ring → stabilization level)

### `app/src/main/java/com/zerotoler/rpgmenu/domain/usecase/BuildBattleTopFromSelectedLoadoutUseCase.kt`

**What it does**

- Builds `BattleTopStats` from player loadout or opponent configuration.
- Computes the **stabilization target** based on the required rules:
  - `DriverClass.CENTRAL` → `StabilizationLevel.CENTER`
  - `DriverClass.CIRCLE` → ring stabilization
    - `RingClass.INNER` or `RingClass.BALANCED` → `INNER_RING`
    - `RingClass.OUTER` or `RingClass.IMBALANCED` → `OUTER_RING`

**Where**

- `stabilizationLevelFor(driverClass, ringClass)`

---

## Domain models used by physics/combat

### `app/src/main/java/com/zerotoler/rpgmenu/domain/model/battle/BattleTopStats.kt`

**What it is**

- The authoritative “build projection” for a top used by the battle engine.
- Includes fields used by physics/combat:
  - `stabilizationLevel`
  - `weightGrams` (clamped 32.5..54.5)
  - `radius`, `mass`
  - `attack`, `defense`, `maxHealth`, `maxStamina`
  - tuning: `balanceFactor`, `wallGrip`, `collisionPower`

### `app/src/main/java/com/zerotoler/rpgmenu/domain/model/battle/StabilizationLevel.kt`

**What it is**

- The stabilization target enum:
  - `CENTER`
  - `INNER_RING`
  - `OUTER_RING`

---

## Rendering & arena visualization (measurements in pixels)

The battle has **two render paths** in the repo:

1) A **SurfaceView** renderer used for gameplay drawing.
2) A **Compose** `Canvas` arena component (kept consistent).

### SurfaceView renderer (runtime)

#### `app/src/main/java/com/zerotoler/rpgmenu/ui/battle/surface/BattleSurfaceView.kt`

**What it does**

- Draws the arena boundary, stabilization rings (red), and tops.

**Arena measurements (pixels)**

- `arenaRadiusPx = minSide * 0.48f`
- `scale = arenaRadiusPx / snap.arenaRadius`

**Stabilization ring rendering (red)**

- Uses radii relative to the drawn arena radius:
  - inner: `arenaR * 0.37f`
  - outer: `arenaR * 0.74f`
- Center marker is a small filled circle near the arena center.

### Compose arena component

#### `app/src/main/java/com/zerotoler/rpgmenu/ui/battle/components/BattleArena.kt`

**What it does**

- Draws an arena background + boundary + stabilization rings (red).
- Also handles pointer input during the launch phase (aim direction).

**Arena measurements (pixels)**

- `arenaRadiusPx = max(minPx * 0.48f, 140.dp)`
- `scale = arenaRadiusPx / snapshot.arenaRadius`

**Stabilization ring rendering (red)**

- inner: `arenaR * 0.37f`
- outer: `arenaR * 0.74f`
- center marker is a small filled circle.

---

## UI / game loop integration (where simulation is driven)

### `app/src/main/java/com/zerotoler/rpgmenu/ui/battle/RealBattleViewModel.kt`

**What it does**

- Creates the **authoritative** engine:
  - `com.zerotoler.rpgmenu.domain.engine.BattleEngine`
- Builds `BattleTopStats` via `BuildBattleTopFromSelectedLoadoutUseCase`
- Exposes snapshots to UI

### `app/src/main/java/com/zerotoler/rpgmenu/ui/battle/surface/BattleSurfaceThread.kt`

**What it does**

- Runs the **fixed timestep loop** for `BattleEngine` and pushes `BattleRenderSnapshot` to UI.

---

## Legacy / alternate physics code (not the runtime engine)

These exist in the repo but are **not** the battle engine currently wired into `RealBattleViewModel`:

### `app/src/main/java/com/zerotoler/rpgmenu/domain/engine/ArenaPhysics.kt`

- A separate movement force model (Vec2-based) for an older simulation path.

### `app/src/main/java/com/zerotoler/rpgmenu/domain/engine/CollisionResolver.kt`

- A Vec2-based collision resolver for circle-circle and wall impacts (non-Dyn4j path).

### `app/src/main/java/com/zerotoler/rpgmenu/domain/engine/DamageResolver.kt`

- Damage formulas for older/non-Dyn4j path.

### `app/src/main/java/com/zerotoler/rpgmenu/domain/engine/battle/BattleEngine.kt`

- A pure float-based “spin-top arena simulation” (separate from Dyn4j engine).

