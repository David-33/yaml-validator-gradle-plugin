package org.yaml.validator.plugin

open class YamlValidatorExtensionProperties(
    var searchPaths: Set<String> = DEFAULT_DIRECTORIES,
    var excludedPaths: Set<String> = emptySet(),
    var searchRecursive: Boolean = true,
    var allowDuplicateKeys: Boolean = false,
    var allowRecursiveKeys: Boolean = false, // https://en.wikipedia.org/wiki/Billion_laughs_attack
    var allowEmptyValues: Boolean = false,
    var processComments: Boolean = false,
    var enumCaseSensitive: Boolean = true,
    var nestingDepthLimit: Int = 50,
    var maxAliasesForCollections: Int = 50, // to prevent YAML at
    var codePointLimit: Int = 3 * 1024 * 1024, // 3 MB
) {
    companion object {
        val DEFAULT_DIRECTORIES = setOf("src/main/resources/", "config/", "consul/")
    }
}
