# TagCore

TagCore is a Hytale server mod/plugin that adds a simple, reusable tag system for game content.

It lets you define named groups of IDs such as items, blocks, entities, biomes, effects, fluids, and damage types, then query those groups through a clean Java API.

## Features

- Define tags in JSON
- Support for these tag types:
    - `item`
    - `block`
    - `entity`
    - `biome`
    - `effect`
    - `fluid`
    - `damage_type`
- Reference one tag from another with `#namespace:path`
- Resolve fully flattened tag contents at startup
- Detect invalid values, missing references, wrong-type references, and circular references
- Load tags from:
    - bundled classpath resources under `tags/`
    - external `.zip` and `.jar` packs from the server `mods/` directory
- Easy-to-use `TagService` API for other mods/plugins

## Installation via Gradle

Add the AzureDoom Maven repository and dependency to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://maven.azuredoom.com/mods")
}

dependencies {
    implementation("com.azuredoom.tagcore:TagCore:1.0.0")
}
```

For Groovy DSL:

```groovy
repositories {
    maven {
        url = uri("https://maven.azuredoom.com/releases")
    }
}

dependencies {
    implementation("com.azuredoom.tagcore:TagCore:1.0.0")
}
```

Maven browser/index: <https://maven.azuredoom.com/#/>

## How Tag IDs Work

Tag IDs use a `namespace:path` format.

Examples:

- `hytale:logs`
- `tagcore:starter_weapons`
- `mymod:undead`

If a tag ID is written without a namespace, TagCore normalizes it to the default namespace:

```text
hytale
```

So a bare tag like:

```text
starter_weapons
```

is treated as:

```text
hytale:starter_weapons
```

## Defining Tags

Tag files are JSON files placed under a `tags/` directory.

A tag definition contains:

- `id` - the tag ID
- `type` - the tag type
- `values` - a list of concrete IDs and/or tag references

### Example item tag

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

### Example block tag

```json
{
  "id": "tagcore:logs",
  "type": "block",
  "values": [
    "Wood_Amber_Trunk",
    "Wood_Ash_Trunk",
    "Wood_Aspen_Trunk"
  ]
}
```

### Example entity tag

```json
{
  "id": "tagcore:undead",
  "type": "entity",
  "values": [
    "Zombie",
    "Skeleton",
    "#tagcore:boss_undead"
  ]
}
```

## Referencing Other Tags

You can include one tag inside another by prefixing the referenced tag ID with `#`.

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

Rules:

- referenced tags must exist
- referenced tags must be the same type
- circular references are rejected

## Where Tags Are Loaded From

TagCore loads tags from two places, in this order:

1. Classpath resources under `tags/`
2. External `.zip` or `.jar` packs in the server `mods/` directory

External packs can override built-in tags with the same ID.

## Using the API

TagCore exposes a shared `TagService` for querying tags.

### Get the shared service

```java
import com.azuredoom.tagcore.api.TagService;

var tagServiceOptional = TagService.getTagService();
if (tagServiceOptional.isEmpty()) {
    return;
}

TagService tagService = tagServiceOptional.get();
```

### Check whether a tag exists

```java
boolean exists = tagService.hasTag("tagcore:starter_weapons");
```

### Resolve a typed tag

```java
var result = tagService.resolveItemTag("tagcore:starter_weapons");

if (result.isSuccess()) {
    for (String itemId : result.value()) {
        System.out.println(itemId);
    }
} else {
    System.out.println("Failed to resolve tag: " + result.status());
    for (var issue : result.issues()) {
        System.out.println(issue.detail());
    }
}
```

### Check membership in a tag

```java
var result = tagService.isInItemTag("tagcore:starter_weapons", "Sword_Wooden");

if (result.isSuccess() && result.value()) {
    System.out.println("Sword_Wooden is in the tag.");
}
```

### Resolve a block tag

```java
var logs = tagService.resolveBlockTag("tagcore:logs");

if (logs.isSuccess()) {
    System.out.println("Resolved block IDs: " + logs.value());
}
```

### Generic membership check

```java
var result = tagService.isInTag("tagcore:starter_weapons", "Sword_Wooden");

if (result.isSuccess()) {
    System.out.println("Contained: " + result.value());
}
```

## Understanding Query Results

Most TagCore API calls return a `TagQueryResult<T>`.

This gives you:

- `status()` - overall outcome
- `value()` - returned value
- `definition()` - matching tag definition, when available
- `issues()` - validation or resolution issues

Common statuses:

- `SUCCESS`
- `EMPTY`
- `NOT_FOUND`
- `WRONG_TYPE`
- `INVALID_TAG_ID`
- `INVALID_CONTENT`
- `CIRCULAR_REFERENCE`

Example:

```java
var result = tagService.resolveBlockTag("tagcore:logs");

switch (result.status()) {
    case SUCCESS, EMPTY -> {
        System.out.println("Resolved values: " + result.value());
    }
    case NOT_FOUND -> {
        System.out.println("Tag not found.");
    }
    default -> {
        System.out.println("Tag resolution failed: " + result.status());
        for (var issue : result.issues()) {
            System.out.println(issue.type() + ": " + issue.detail());
        }
    }
}
```

## Recommended Folder Layout

Example bundled resource layout:

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

The exact subfolder structure under `tags/` is flexible. What matters is that the files are discoverable under the `tags/` root.

## Example Pack Override

You can ship a default tag in your mod, then override it from an external pack in `mods/`.

Bundled tag:

```json
{
  "id": "tagcore:starter_weapons",
  "type": "item",
  "values": [
    "Sword_Wooden"
  ]
}
```

External override:

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

## Notes

- Values must be valid IDs for the declared type.
- References must point to tags of the same type.
- TagCore resolves references eagerly so configuration problems show up early.
- Bare tag IDs are normalized to the default namespace `hytale`.

## License

MIT