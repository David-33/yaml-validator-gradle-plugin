package org.yaml.validator.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.events.ScalarEvent
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.pathString

open class YamlValidatorTask : DefaultTask() {

    private var properties = project.extensions.findByType(YamlValidatorExtensionProperties::class.java)!!
    private var yaml: Yaml

    init {
        logger.info(properties.toString())
        val loaderOptions = LoaderOptions().apply {
            isAllowDuplicateKeys = properties.allowDuplicateKeys
            allowRecursiveKeys = properties.allowRecursiveKeys
            isProcessComments = properties.processComments
            isEnumCaseSensitive = properties.enumCaseSensitive
            nestingDepthLimit = properties.nestingDepthLimit
            maxAliasesForCollections = properties.maxAliasesForCollections
            codePointLimit = properties.codePointLimit
        }
        yaml = Yaml(loaderOptions)
    }

    @TaskAction
    fun validateAllProvidedFilesAndDirectories() {
        properties.excludedPaths = properties.excludedPaths
            .mapNotNull(this::resolveFileOrDirectoryByPath)
            .mapTo(mutableSetOf(), Path::toString)

        properties.searchPaths
            .mapNotNull(this::resolveFileOrDirectoryByPath)
            .forEach(this::checkFileOrDirectory)
    }

    private fun resolveFileOrDirectoryByPath(path: String): Path? =
        try {
            project.file(path).toPath().toAbsolutePath().toRealPath()
        } catch (e: IOException) {
            logger.warn(e.toString())
            null
        }

    private fun checkFileOrDirectory(fileOrDirectory: Path) {
        if (Files.isDirectory(fileOrDirectory)) {
            validateDirectory(fileOrDirectory)
        } else if (isYamlFileAndNotExcluded(fileOrDirectory)) {
            validateYamlFile(fileOrDirectory)
        } else {
            throw IOException("File at path $fileOrDirectory is neither a file nor a directory.")
        }
    }

    private fun validateDirectory(directory: Path) {
        if (properties.searchRecursive) {
            validateYamlFilesInDirectoryRecursively(directory)
        } else {
            validateYamlFilesOnlyDirectlyInDirectory(directory)
        }
    }

    private fun validateYamlFilesInDirectoryRecursively(directory: Path) {
        logger.info(String.format(STARTING_DIRECTORY_RECURSIVE_MESSAGE, directory))
        Files.walk(directory)
            .filter(this::isYamlFileAndNotExcluded)
            .forEach(this::validateYamlFile)
    }

    private fun validateYamlFilesOnlyDirectlyInDirectory(directory: Path) {
        logger.info(String.format(STARTING_DIRECTORY_MESSAGE, directory))
        Files.list(directory)
            .filter(this::isYamlFileAndNotExcluded)
            .forEach(this::validateYamlFile)
    }

    private fun isYamlFileAndNotExcluded(file: Path): Boolean {
        if (!Files.isRegularFile(file) || properties.excludedPaths.any { file.startsWith(it) }) return false
        val fileName = file.fileName?.toString() ?: error("Couldn't extract file name from $file.")
        return fileName.endsWith(".yaml") || fileName.endsWith(".yml")
    }

    @Suppress("TooGenericExceptionCaught")
    private fun validateYamlFile(file: Path) {
        logger.info(String.format(STARTING_FILE_MESSAGE, file))
        try {
            validateAllDocuments(file)
        } catch (e: Exception) {
            throw GradleException(String.format(FILE_FAILURE_MESSAGE, file), e)
        }
        logger.info(String.format(FILE_SUCCESS_MESSAGE, file))
    }

    private fun validateAllDocuments(file: Path) {
        val buffer = ByteArrayOutputStream()
        Files.newInputStream(file).use { it.transferTo(buffer) }
        val inputStream: InputStream = ByteArrayInputStream(buffer.toByteArray())

        if (!properties.allowEmptyValues) {
            yaml.parse(InputStreamReader(inputStream)).forEach { event ->
                if (event is ScalarEvent && event.value.isNullOrEmpty()) {
                    error(
                        "empty value in ${file.pathString}, line ${event.startMark.line + 1}, column ${event.startMark.column + 1}:\n" +
                            event.startMark._snippet
                    )
                }
            }
            inputStream.reset()
        }

        yaml.loadAll(inputStream).forEachIndexed { index, _ ->
            logger.info(String.format(DOCUMENT_SUCCESS_MESSAGE, index + 1, file))
        }
    }

    companion object {
        private const val STARTING_DIRECTORY_MESSAGE = "Starting validation of YAML files in directory '%s'."
        private const val STARTING_DIRECTORY_RECURSIVE_MESSAGE = "Starting validation of YAML files in directory '%s' recursively."
        private const val STARTING_FILE_MESSAGE = "Starting validation of YAML file '%s'."
        private const val DOCUMENT_SUCCESS_MESSAGE = "Validation of document #%s in file %s successful."
        private const val FILE_SUCCESS_MESSAGE = "Validation of YAML file '%s' successful."
        private const val FILE_FAILURE_MESSAGE = "Validation of YAML file '%s' failed."
    }
}
