# yaml-validator-gradle-plugin

#### Gradle plugin for yaml/yml files validation

---

### How to use

**Plugin connection:**

```kotlin
plugins {
    id("org.yaml.validator.plugin.yaml-validator-plugin") version "0.1.0"
}
```

**Params and default values:**

```kotlin
searchPaths = ["src/main/resources/", "config/", "consul/"] // searching directories (you can also specify a specific file)
excludedPaths = [] // ignoring directories, for exclude from `searchPaths` (you can also specify a specific file)
searchRecursive = true // enable/disable recursive search in `searchPaths`
allowDuplicateKeys = false // allow duplicate keys in yaml files
allowRecursiveKeys = false // allow recursive keys in yaml files (https://en.wikipedia.org/wiki/Billion_laughs_attack)
allowEmptyValues = false // allow empty values in "leafs"
processComments = false // process comments in yaml files
enumCaseSensitive = true // case-sensitive processing of enums
nestingDepthLimit = 50 // max depth of keys tree in yaml files
maxAliasesForCollections = 50 // max alias-refs, to avoid this: https://en.wikipedia.org/wiki/Billion_laughs_attack
codePointLimit = 3 * 1024 * 1024 // max code-point (symbols) count in each yaml file
```

**Custom configuration example:**

```kotlin
yamlValidatorExtension {
    searchPaths = setOf("config/", "consul/")
    excludedPaths = setOf("config/detekt/")
    allowEmptyValues = true
}
```

---

### Using

You can run the check using the gradle task called _validateYaml_, it also depends on the _detekt_ and _check_ tasks

---

### Owners:

- [David Tadevosyan](https://github.com/David-33)
