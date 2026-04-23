---
title: "Tag Loading"
order: 4
published: true
draft: false
---

# Tag Loading

This page explains how TagCore discovers, registers, resolves, and overrides tag definitions at startup.

## Overview

At a high level, TagCore loads built-in and classpath tags first, then loads external packs from the server `mods/` directory, and finally resolves tag references into flattened value sets.

```mermaid
flowchart TD
    A[Server starts] --> B[Load classpath resources under tags/]
    B --> C[Register built-in tag definitions]
    C --> D[Scan mods/ for .zip and .jar packs]
    D --> E[Sort pack filenames alphabetically]
    E --> F[Load external tag definitions]
    F --> G[Resolve #references recursively]
    G --> H[Flatten to concrete value sets]
    H --> I[Cache resolved tags for access]
```

## Load Order

TagCore loads tags in this order:

1. Classpath resources under `tags/`
2. External `.zip` and `.jar` packs from the server `mods/` directory

External packs are processed in **alphabetical filename order**.

```mermaid
flowchart LR
    A[Classpath tags] --> B[Built-in registry state]
    B --> C[External pack A.zip]
    C --> D[External pack B.zip]
    D --> E[External pack C.jar]
    E --> F[Final loaded definitions]
```

## Override Rules

### Built-in and classpath tags

If multiple built-in or classpath tag definitions use the same tag ID, the first one loaded is kept and later duplicates are skipped with a warning.

### External packs

External packs load after built-ins and may override existing tags with the same ID.

Because external packs are processed in ascending filename order, later-sorting pack files have final priority when multiple packs define the same tag.

## Effective Precedence

TagCore resolves precedence using this model:

1. Earliest built-in/classpath definition wins among built-ins.
2. External packs override built-ins.
3. Among external packs, the last pack in ascending filename order wins.

```mermaid
flowchart TD
    A[Built-in tag: first definition found] --> B{Same tag ID exists later?}
    B -- Yes, built-in/classpath --> C[Skip later duplicate and warn]
    B -- Yes, external pack --> D[Override existing definition]
    B -- No --> E[Keep current definition]
    D --> F{Another later-sorting external pack?}
    F -- Yes --> G[Override again]
    F -- No --> H[Final active definition]
    C --> H
    E --> H
    G --> H
```

## Resolution Behavior

After tags are collected, TagCore resolves `#references` recursively and flattens them into concrete value sets.

This happens eagerly so that configuration problems appear during startup instead of later during gameplay.

```mermaid
flowchart TD
    A[Tag definition loaded] --> B{Contains direct values?}
    B -- Yes --> C[Validate values for tag type]
    B -- No --> D
    C --> D{Contains #references?}
    D -- Yes --> E[Load referenced tag]
    E --> F[Verify referenced tag exists]
    F --> G[Verify referenced tag type matches]
    G --> H[Resolve nested references recursively]
    H --> I[Merge referenced values]
    D -- No --> J[Finalize resolved value set]
    I --> J
    J --> K[Cache resolved result]
```

## Validation

TagCore detects these classes of problems while resolving tags:

- Invalid content values
- Missing tag references
- Wrong-type references
- Circular references
- Invalid tag IDs

```mermaid
flowchart TD
    A[Resolve tag] --> B{Tag ID valid?}
    B -- No --> X1[Fail startup]
    B -- Yes --> C{Values valid for type?}
    C -- No --> X2[Fail startup]
    C -- Yes --> D{All references exist?}
    D -- No --> X3[Fail startup]
    D -- Yes --> E{Reference types match?}
    E -- No --> X4[Fail startup]
    E -- Yes --> F{Circular reference detected?}
    F -- Yes --> X5[Fail startup]
    F -- No --> G[Resolution succeeds]
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
    A[Bundled definition\ntagcore:starter_weapons] --> C[Active tag]
    B[External pack definition\ntagcore:starter_weapons] --> C
    C --> D[External definition wins]
```

## Notes

- Values must be valid IDs for the declared tag type.
- References must point to tags of the same type.
- Resolved values are cached after first access.
- TagCore is designed to fail fast on broken tag definitions.
