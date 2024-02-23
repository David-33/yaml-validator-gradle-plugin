package org.yaml.validator.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

@Suppress("Unused", "StringLiteralDuplication")
class YamlValidatorPlugin : Plugin<Project> {

    private var yamlValidatorTask: YamlValidatorTask? = null

    override fun apply(target: Project) {
        target.extensions.create(VALIDATOR_EXTENSION_NAME, YamlValidatorExtensionProperties::class.java)
        yamlValidatorTask = target.tasks.create(VALIDATE_YAML_TASK_NAME, YamlValidatorTask::class.java)

        val detektTasks = target.getTasksByName("detekt", false)
        val checkTasks = target.getTasksByName("check", false)

        if (detektTasks.isNotEmpty()) {
            target.logger.info(
                "Found detekt tasks: $detektTasks" +
                        "\n\tmaking them depend on ':$VALIDATE_YAML_TASK_NAME' task"
            )
            detektTasks.forEach(this::makeTaskDependOnYamlValidatorTask)
        }
        if (checkTasks.isNotEmpty()) {
            target.logger.info(
                "Found check tasks: $checkTasks" +
                        "\n\tmaking them depend on ':$VALIDATE_YAML_TASK_NAME' task"
            )
            checkTasks.forEach(this::makeTaskDependOnYamlValidatorTask)
        }
    }

    private fun makeTaskDependOnYamlValidatorTask(task: Task) =
        yamlValidatorTask?.let { task.dependsOn(it) }

    companion object {
        private const val VALIDATOR_EXTENSION_NAME = "yamlValidatorExtension"
        private const val VALIDATE_YAML_TASK_NAME = "validateYaml"
    }
}
