package ru.bash.semantic

import ru.bash.syntax.ast.ArgumentNode
import ru.bash.syntax.ast.ArithmeticExpansionNode
import ru.bash.syntax.ast.CommandSubstitutionNode
import ru.bash.syntax.ast.ExitStatusNode
import ru.bash.syntax.ast.ShellWordNode
import ru.bash.syntax.ast.SingleQuotedNode
import ru.bash.syntax.ast.StringNode
import ru.bash.syntax.ast.VariableNode
import ru.bash.syntax.ast.WordNode

class ShellWordExpander(
    private val context: ExpansionContext,
    private val substitution: CommandSubstitutionRunner,
) {

    suspend fun expand(node: ArgumentNode): String = when (node) {
        is WordNode -> node.value
        is StringNode -> node.value
        is SingleQuotedNode -> node.value
        is VariableNode -> context.environment[node.value] ?: ""
        is ExitStatusNode -> context.lastExitCode.toString()
        is ArithmeticExpansionNode -> ArithmeticEvaluator.eval(node.expression).toString()
        is CommandSubstitutionNode -> substitution.runSubstitution(node.inner)
        is ShellWordNode -> {
            val sb = StringBuilder()
            for (part in node.parts) {
                sb.append(expand(part))
            }
            sb.toString()
        }
    }
}
