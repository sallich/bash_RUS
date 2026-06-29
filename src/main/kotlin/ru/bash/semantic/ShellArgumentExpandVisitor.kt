package ru.bash.semantic

import ru.bash.syntax.ast.AssignNode
import ru.bash.syntax.ast.AstVisitor
import ru.bash.syntax.ast.CommandNode
import ru.bash.syntax.ast.PipelineNode
import ru.bash.syntax.ast.ShellWordNode
import ru.bash.syntax.ast.SingleQuotedNode
import ru.bash.syntax.ast.StringNode
import ru.bash.syntax.ast.VariableNode
import ru.bash.syntax.ast.WordNode

class ShellArgumentExpandVisitor(
    private val environment: Map<String, String>
) : AstVisitor<String> {

    override fun visitPipeline(node: PipelineNode): String =
        throw UnsupportedOperationException("Expected argument node")

    override fun visitCommand(node: CommandNode): String =
        throw UnsupportedOperationException("Expected argument node")

    override fun visitAssign(node: AssignNode): String =
        throw UnsupportedOperationException("Expected argument node")

    override fun visitShellWord(node: ShellWordNode): String =
        node.parts.joinToString(separator = "") { it.accept(this) }

    override fun visitWord(node: WordNode): String = node.value

    override fun visitString(node: StringNode): String = node.value

    override fun visitSingleQuoted(node: SingleQuotedNode): String = node.value

    override fun visitVariable(node: VariableNode): String =
        environment[node.value] ?: ""
}
