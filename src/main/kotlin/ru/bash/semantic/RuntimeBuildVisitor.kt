package ru.bash.semantic

import ru.bash.semantic.model.ExecCommand
import ru.bash.semantic.model.ExecPipeline
import ru.bash.syntax.ast.AssignNode
import ru.bash.syntax.ast.AstVisitor
import ru.bash.syntax.ast.CommandNode
import ru.bash.syntax.ast.PipelineNode
import ru.bash.syntax.ast.ShellWordNode
import ru.bash.syntax.ast.SingleQuotedNode
import ru.bash.syntax.ast.StringNode
import ru.bash.syntax.ast.VariableNode
import ru.bash.syntax.ast.WordNode

class RuntimeBuildVisitor(
    environment: Map<String, String>
) : AstVisitor<Unit> {

    private val argVisitor = ShellArgumentExpandVisitor(environment)
    private val commands = mutableListOf<ExecCommand>()

    fun build(pipeline: PipelineNode): ExecPipeline {
        commands.clear()
        pipeline.accept(this)
        return ExecPipeline(commands.toList())
    }

    override fun visitPipeline(node: PipelineNode) {
        node.nodes.forEach { it.accept(this) }
    }

    override fun visitCommand(node: CommandNode) {
        val argv = mutableListOf<String>()

        val expandedName = node.name.accept(argVisitor)
        if (expandedName.isNotEmpty()) {
            argv += expandedName
        }

        node.nodes
            .map { it.accept(argVisitor) }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { argv += it }

        commands += ExecCommand(argv)
    }

    override fun visitAssign(node: AssignNode): Unit =
        throw UnsupportedOperationException("AssignNode must be handled before build()")

    override fun visitShellWord(node: ShellWordNode): Unit =
        throw UnsupportedOperationException("Expected pipeline or command")

    override fun visitWord(node: WordNode): Unit =
        throw UnsupportedOperationException("Expected pipeline or command")

    override fun visitString(node: StringNode): Unit =
        throw UnsupportedOperationException("Expected pipeline or command")

    override fun visitSingleQuoted(node: SingleQuotedNode): Unit =
        throw UnsupportedOperationException("Expected pipeline or command")

    override fun visitVariable(node: VariableNode): Unit =
        throw UnsupportedOperationException("Expected pipeline or command")
}
