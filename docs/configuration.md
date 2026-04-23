---
title: "Configuration"
order: 3
published: true
draft: false
---

# Configuration

TagCore does not use a traditional settings JSON for its main behavior. Instead, it is configured through **tag definition files** stored under a `tags/` root.

## Where Tags Are Loaded From

TagCore loads tags from two sources:

1. **Bundled classpath resources** under `tags/`
2. **External `.zip` and `.jar` packs** in the server `mods/` directory

Bundled resources are useful for shipping default tags with your mod. External packs are useful for overrides and server-side customization.

## Tag File Format

Each tag file is a JSON object with three core fields:

- `id` - the tag identifier
- `type` - the tag type
- `values` - a list of concrete IDs and/or tag references

### Example

```json
{
  "id": "tagcore:starter_weapons",
  "type": "item",
  "values": [
    "Weapon_Sword_Wood",
    "Weapon_Shortbow_Crude",
    "#tagcore:starter_ammo"
  ]
}
```

## Tag ID Format

Tag IDs use the format:

```text
namespace:path
```

### Examples

- `hytale:logs`
- `tagcore:starter_weapons`
- `mymod:undead`

If a tag ID is written without a namespace, TagCore normalizes it to the default namespace:

```text
hytale
```

So this:

```text
starter_weapons
```

is treated as:

```text
hytale:starter_weapons
```

## Supported Tag Types

TagCore currently supports these tag types:

- `item`
- `block`
- `entity`
- `biome`
- `effect`
- `fluid`
- `damage_type`

Type names are parsed case-insensitively, and surrounding whitespace is ignored.

## Tag References

You can reference one tag from another by prefixing the target tag ID with `#`.

### Example

```json
{
  "id": "tagcore:all_logs",
  "type": "block",
  "values": [
    "#tagcore:logs",
    "#tagcore:modded_logs"
  ]
}
```

### Rules

- Referenced tags must exist.
- Referenced tags must be the same type as the tag containing the reference.
- Circular references are rejected.

## Recommended Folder Layout

A typical bundled layout looks like this:

```text
src/main/resources/
└── tags/
    ├── items/
    │   └── starter_weapons.json
    ├── blocks/
    │   └── logs.json
    └── entities/
        └── undead.json
```

The exact folder structure under `tags/` is flexible. The important part is that files are discoverable somewhere under the `tags/` root.

## Best Practices

- Keep one tag definition per file.
- Use clear namespaces for your own mod content.
- Prefer bundled defaults and external overrides instead of editing shipped jars.
- Validate referenced tags carefully when composing larger tag groups.
