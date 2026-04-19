package ru.bash.semantic

import ru.bash.semantic.model.ExecCommand
import ru.bash.semantic.model.ExecPipeline
import ru.bash.syntax.ast.CommandNode
import ru.bash.syntax.ast.PipelineNode

class RuntimeBuildVisitor(
    private val expander: ShellWordExpander,
) {

    suspend fun build(pipeline: PipelineNode): ExecPipeline {
        val commands = pipeline.nodes.map { expandCommand(it) }
        return ExecPipeline(commands)
    }

    private suspend fun expandCommand(node: CommandNode): ExecCommand {
        val argv = mutableListOf<String>()

        val expandedName = expander.expand(node.name)
        if (expandedName.isNotEmpty()) {
            argv += expandedName
        }

        node.nodes
            .map { expander.expand(it).trim() }
            .filter { it.isNotEmpty() }
            .forEach { argv += it }

        return ExecCommand(argv)
    }
}
