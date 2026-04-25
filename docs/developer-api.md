---
title: "Developer API"
order: 5
published: true
draft: false
---

# Developer API

This page explains how to integrate with TagCore from another Hytale mod or plugin.

# Adding the Maven Repository

TagCore is hosted on the AzureDoom Maven.
Add the following repositories block to your build.gradle file:
```gradle
repositories {
    maven {
        name = "azuredoomMods"
        url = uri("https://maven.azuredoom.com/mods")
    }
}
```

# Adding TagCore as a Dependency

Once the repository is added, include TagCore as a dependency:

```gradle
dependencies {
    implementation("com.azuredoom.tagcore:TagCore:1.+")
}
```

Now in your mods/plugins `manifest.json` add `"com.azuredoom:tagcore": "*"` to either `Dependencies` to set as hard or `OptionalDependencies` if you are adding integration like so:

```json
{
    // rest of manifest.json
    "Dependencies": {
        "com.azuredoom:tagcore": "*"
    },
    "OptionalDependencies": {
        "com.azuredoom:tagcore": "*"
    },
    // rest of manifest.json
}
```

## Recommended Entry Point

TagCore exposes a shared `TagService` as the primary public API.

`TagService` is the recommended integration layer because it provides a stable facade over the internal registry.

## Getting the Shared Service

```java
import com.azuredoom.tagcore.api.TagService;

var tagServiceOptional = TagService.getTagService();
if (tagServiceOptional.isEmpty()) {
    return;
}

TagService tagService = tagServiceOptional.get();
```

## Checking Whether a Tag Exists

```java
boolean exists = tagService.hasTag("tagcore:starter_weapons");
```

## Getting All Registered Tags

```java
var tags = tagService.allTags();
for (var tag : tags) {
    System.out.println(tag.id());
}
```

## Resolving a Typed Tag

### Item tag

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

### Block tag

```java
var logs = tagService.resolveBlockTag("tagcore:logs");
if (logs.isSuccess()) {
    System.out.println("Resolved block IDs: " + logs.value());
}
```

## Membership Checks

### Generic membership check

```java
var result = tagService.isInTag("tagcore:starter_weapons", "Sword_Wooden");
if (result.isSuccess()) {
    System.out.println("Contained: " + result.value());
}
```

### Typed membership check

```java
var result = tagService.isInItemTag("tagcore:starter_weapons", "Sword_Wooden");
if (result.isSuccess() && result.value()) {
    System.out.println("Sword_Wooden is in the tag.");
}
```

## Understanding Query Results

Most TagCore API calls return a `TagQueryResult`.

A query result includes:

- `status()` - the overall result state
- `value()` - the returned value
- `definition()` - the matching tag definition, when available
- `issues()` - validation or resolution problems

### Common statuses

- `SUCCESS`
- `EMPTY`
- `NOT_FOUND`
- `WRONG_TYPE`
- `INVALID_TAG_ID`
- `INVALID_CONTENT`
- `CIRCULAR_REFERENCE`

### Example result handling

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

## Best Practices

- Use `TagService` instead of depending on internal implementation details.
- Treat `TagService.getTagService()` as optional during initialization.
- Prefer typed resolution methods when you know the expected tag type.
- Handle failure statuses instead of assuming all tags resolve successfully.
