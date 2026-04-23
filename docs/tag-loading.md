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
%%{init: {
  "theme": "base",
  "themeVariables": {
    "primaryColor": "#4F46E5",
    "primaryTextColor": "#FFFFFF",
    "primaryBorderColor": "#3730A3",
    "lineColor": "#475569",
    "secondaryColor": "#0EA5E9",
    "tertiaryColor": "#F8FAFC",
    "background": "#FFFFFF",
    "mainBkg": "#EEF2FF",
    "secondBkg": "#E0F2FE",
    "tertiaryBkg": "#F8FAFC"
  }
}}%%
flowchart LR
    A[Discover built-in and classpath tags] --> B[Load external zip and jar packs]
    B --> C[Register or override tag definitions]
    C --> D[Resolve #references recursively]
    D --> E[Flatten to concrete values]
    E --> F[Cache resolved values for runtime access]

    classDef discovery fill:#DBEAFE,stroke:#2563EB,color:#111827,stroke-width:2px;
    classDef external fill:#E0F2FE,stroke:#0284C7,color:#111827,stroke-width:2px;
    classDef registry fill:#EDE9FE,stroke:#7C3AED,color:#111827,stroke-width:2px;
    classDef resolve fill:#FEF3C7,stroke:#D97706,color:#111827,stroke-width:2px;
    classDef cache fill:#DCFCE7,stroke:#16A34A,color:#111827,stroke-width:2px;

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
%%{init: {
  "theme": "base",
  "themeVariables": {
    "primaryColor": "#4F46E5",
    "primaryTextColor": "#FFFFFF",
    "primaryBorderColor": "#3730A3",
    "lineColor": "#475569",
    "background": "#FFFFFF"
  }
}}%%
flowchart TD
    A[Classpath tags under tags/] --> B[mods/A-pack.zip]
    B --> C[mods/MidPack.jar]
    C --> D[mods/ZOverride.zip]

    classDef builtIn fill:#EDE9FE,stroke:#7C3AED,color:#111827,stroke-width:2px;
    classDef external fill:#DBEAFE,stroke:#2563EB,color:#111827,stroke-width:2px;
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
%%{init: {
  "theme": "base",
  "themeVariables": {
    "lineColor": "#475569",
    "background": "#FFFFFF"
  }
}}%%
flowchart TD
    A[Built-in definition] --> B{Same tag ID exists later?}
    B -->|No| C[Keep built-in]
    B -->|Yes, in built-ins only| D[Keep first built-in and warn]
    B -->|Yes, in external pack| E[External pack replaces prior definition]
    E --> F{Another later external pack?}
    F -->|No| G[Final effective definition]
    F -->|Yes| H[Later alphabetical pack wins]
    H --> G

    classDef question fill:#F3F4F6,stroke:#6B7280,color:#111827,stroke-width:2px;
    classDef keep fill:#DCFCE7,stroke:#16A34A,color:#111827,stroke-width:2px;
    classDef warn fill:#FEF3C7,stroke:#D97706,color:#111827,stroke-width:2px;
    classDef override fill:#FEE2E2,stroke:#DC2626,color:#111827,stroke-width:2px;

    class B,F question;
    class C,G keep;
    class D warn;
    class E,H override;
```

## Resolution Behavior

After tags are collected, TagCore resolves `#references` recursively and flattens them into concrete value sets.

This happens eagerly so that configuration problems appear during startup instead of later during gameplay.

```mermaid
%%{init: {
  "theme": "base",
  "themeVariables": {
    "lineColor": "#475569",
    "background": "#FFFFFF"
  }
}}%%
flowchart TD
    A[Read tag values] --> B{Literal value or #reference?}
    B -->|Literal value| C[Add value to resolved set]
    B -->|#reference| D[Load referenced tag]
    D --> E{Same tag type?}
    E -->|Yes| F[Resolve recursively]
    F --> C
    E -->|No| G[Raise validation error]

    classDef input fill:#DBEAFE,stroke:#2563EB,color:#111827,stroke-width:2px;
    classDef question fill:#F3F4F6,stroke:#6B7280,color:#111827,stroke-width:2px;
    classDef success fill:#DCFCE7,stroke:#16A34A,color:#111827,stroke-width:2px;
    classDef error fill:#FEE2E2,stroke:#DC2626,color:#111827,stroke-width:2px;

    class A,D input;
    class B,E question;
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
%%{init: {
  "theme": "base",
  "themeVariables": {
    "lineColor": "#475569",
    "background": "#FFFFFF"
  }
}}%%
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

    classDef process fill:#EDE9FE,stroke:#7C3AED,color:#111827,stroke-width:2px;
    classDef accept fill:#DCFCE7,stroke:#16A34A,color:#111827,stroke-width:2px;
    classDef fail fill:#FEE2E2,stroke:#DC2626,color:#111827,stroke-width:2px;

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
%%{init: {
  "theme": "base",
  "themeVariables": {
    "lineColor": "#475569",
    "background": "#FFFFFF"
  }
}}%%
flowchart LR
    A[Bundled starter_weapons] --> B[External starter_weapons]
    B --> C[Effective final tag]

    classDef bundled fill:#EDE9FE,stroke:#7C3AED,color:#111827,stroke-width:2px;
    classDef external fill:#DBEAFE,stroke:#2563EB,color:#111827,stroke-width:2px;
    classDef final fill:#DCFCE7,stroke:#16A34A,color:#111827,stroke-width:2px;

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
