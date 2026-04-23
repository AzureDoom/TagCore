---
title: "Tag Loading"
order: 4
published: true
draft: false
---

# Tag Loading

This page explains how TagCore discovers, registers, resolves, and overrides tag definitions at startup.

## Overview

Tag loading follows a predictable pipeline:

```mermaid
flowchart LR
    A[Discover built-in and classpath tags] --> B[Load external zip and jar packs]
    B --> C[Register or override tag definitions]
    C --> D[Resolve #references recursively]
    D --> E[Flatten to concrete values]
    E --> F[Cache resolved values for runtime access]

%% Classes
    classDef discovery fill:#4A90E2,color:#fff,stroke:#2C5FA3,stroke-width:2px;
    classDef external fill:#50B4E6,color:#fff,stroke:#2A7FA8,stroke-width:2px;
    classDef registry fill:#9013FE,color:#fff,stroke:#5E0CB2,stroke-width:2px;
    classDef resolve fill:#F5A623,color:#000,stroke:#C97A00,stroke-width:2px;
    classDef cache fill:#27AE60,color:#fff,stroke:#1E8449,stroke-width:2px;

%% Apply classes
    class A discovery;
    class B external;
    class C registry;
    class D,E resolve;
    class F cache;
```

## Load Order

TagCore loads tags in this order:

1. Classpath resources under `tags/`
2. External `.zip` and `.jar` packs from the server `mods/` directory

External packs are processed in **alphabetical filename order**.

```mermaid
flowchart TD
    A[Classpath tags under tags/] --> B[mods/A-pack.zip]
B --> C[mods/MidPack.jar]
C --> D[mods/ZOverride.zip]

%% Classes
classDef builtIn fill:#9013FE,color:#fff,stroke:#5E0CB2,stroke-width:2px;
classDef external fill:#4A90E2,color:#fff,stroke:#2C5FA3,stroke-width:2px;

%% Apply classes
class A builtIn;
class B,C,D external;
```

## Override Rules

### Built-in and classpath tags

If multiple built-in or classpath tag definitions use the same tag ID, the first one loaded is kept and later duplicates are skipped with a warning.

### External packs

External packs load after built-ins and may override existing tags with the same ID.

Because external packs are processed in ascending filename order, later-sorting pack files have final priority when multiple packs define the same tag.

## Effective Precedence

1. Earliest built-in/classpath definition wins among built-ins.
2. External packs override built-ins.
3. Among external packs, the last pack in ascending filename order wins.

```mermaid
flowchart TD
    A[Built-in definition] --> B{Same tag ID exists later?}
    B -- No --> C[Keep built-in]
    B -- Yes, in built-ins only --> D[Keep first built-in and warn]
    B -- Yes, in external pack --> E[External pack replaces prior definition]
    E --> F{Another later external pack?}
    F -- No --> G[Final effective definition]
    F -- Yes --> H[Later alphabetical pack wins]
    H --> G

%% Classes
    classDef builtIn fill:#9013FE,color:#fff,stroke:#5E0CB2,stroke-width:2px;
    classDef decision fill:#F5A623,color:#000,stroke:#C97A00,stroke-width:2px;
    classDef keep fill:#27AE60,color:#fff,stroke:#1E8449,stroke-width:2px;
    classDef warn fill:#FFD166,color:#000,stroke:#C97A00,stroke-width:2px;
    classDef override fill:#E74C3C,color:#fff,stroke:#A93226,stroke-width:2px;

%% Apply classes
    class A builtIn;
    class B,F decision;
    class C,G keep;
    class D warn;
    class E,H override;
```

## Resolution Behavior

After tags are collected, TagCore resolves `#references` recursively and flattens them into concrete value sets.

This happens eagerly so that configuration problems appear during startup instead of later during gameplay.

```mermaid
flowchart TD
    A[Read tag values] --> B{Literal value or #reference?}
    B -- Literal value --> C[Add value to resolved set]
    B -- #reference --> D[Load referenced tag]
    D --> E{Same tag type?}
    E -- Yes --> F[Resolve recursively]
    F --> C
    E -- No --> G[Raise validation error]

%% Classes
    classDef input fill:#4A90E2,color:#fff,stroke:#2C5FA3,stroke-width:2px;
    classDef decision fill:#F5A623,color:#000,stroke:#C97A00,stroke-width:2px;
    classDef success fill:#27AE60,color:#fff,stroke:#1E8449,stroke-width:2px;
    classDef error fill:#E74C3C,color:#fff,stroke:#A93226,stroke-width:2px;

%% Apply classes
    class A,D input;
    class B,E decision;
    class C,F success;
    class G error;
```

## Validation

TagCore detects these classes of problems while resolving tags:

- Invalid content values
- Missing tag references
- Wrong-type references
- Circular references
- Invalid tag IDs

```mermaid
flowchart LR
    A[Parse tag file] --> B[Validate tag ID]
    B --> C[Validate values]
    C --> D[Validate references]
    D --> E[Detect circular dependencies]
    E --> F[Accept tag]

    B -. invalid ID .-> G[Startup error]
    C -. invalid value .-> G
    D -. missing or wrong type .-> G
    E -. cycle detected .-> G

%% Classes
    classDef process fill:#4A90E2,color:#fff,stroke:#2C5FA3,stroke-width:2px;
    classDef accept fill:#27AE60,color:#fff,stroke:#1E8449,stroke-width:2px;
    classDef fail fill:#E74C3C,color:#fff,stroke:#A93226,stroke-width:2px;

%% Apply classes
    class A,B,C,D,E process;
    class F accept;
    class G fail;
```

## Example Override

### Bundled tag

```json
{
  "id": "tagcore:starter_weapons",
  "type": "item",
  "values": [
    "Sword_Wooden"
  ]
}
```

### External override

```json
{
  "id": "tagcore:starter_weapons",
  "type": "item",
  "values": [
    "Sword_Wooden",
    "Bow_Basic",
    "Dagger_Rusty"
  ]
}
```

In this case, the external version replaces the bundled version because it loads later.

```mermaid
flowchart LR
    A[Bundled starter_weapons] --> B[External starter_weapons]
    B --> C[Effective final tag]

%% Classes
    classDef bundled fill:#9013FE,color:#fff,stroke:#5E0CB2,stroke-width:2px;
    classDef external fill:#4A90E2,color:#fff,stroke:#2C5FA3,stroke-width:2px;
    classDef final fill:#27AE60,color:#fff,stroke:#1E8449,stroke-width:2px;

%% Apply classes
    class A bundled;
    class B external;
    class C final;
```

## Notes

- Values must be valid IDs for the declared tag type.
- References must point to tags of the same type.
- Resolved values are cached after first access.
- TagCore is designed to fail fast on broken tag definitions.

## Mermaid Color Notes

The diagrams above use Mermaid `init` blocks and `classDef` styling. GitHub's Mermaid renderer usually supports these, but exact rendering can vary slightly between GitHub, static-site generators, and local preview tools.
