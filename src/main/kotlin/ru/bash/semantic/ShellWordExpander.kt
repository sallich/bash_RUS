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
        is ArithmeticExpansionNode -> {
            val preparedExpression = expandArithmeticExpression(node.expression)
            ArithmeticEvaluator.eval(preparedExpression).toString()
        }
        is CommandSubstitutionNode -> substitution.runSubstitution(node.inner)
        is ShellWordNode -> {
            val sb = StringBuilder()
            for (part in node.parts) {
                sb.append(expand(part))
            }
            sb.toString()
        }
    }

    private fun expandArithmeticExpression(expression: String): String {
        val out = StringBuilder()
        var i = 0
        while (i < expression.length) {
            val c = expression[i]
            when {
                c == '$' && i + 1 < expression.length && isIdentifierStart(expression[i + 1]) -> {
                    var j = i + 2
                    while (j < expression.length && isIdentifierPart(expression[j])) {
                        j++
                    }
                    val name = expression.substring(i + 1, j)
                    out.append(resolveArithmeticVariable(name))
                    i = j
                }
                isIdentifierStart(c) -> {
                    var j = i + 1
                    while (j < expression.length && isIdentifierPart(expression[j])) {
                        j++
                    }
                    val name = expression.substring(i, j)
                    out.append(resolveArithmeticVariable(name))
                    i = j
                }
                else -> {
                    out.append(c)
                    i++
                }
            }
        }
        return out.toString()
    }

    private fun resolveArithmeticVariable(name: String): Long =
        context.environment[name]?.trim()?.toLongOrNull() ?: 0L

    private fun isIdentifierStart(c: Char): Boolean = c == '_' || c.isLetter()

    private fun isIdentifierPart(c: Char): Boolean = c == '_' || c.isLetterOrDigit()
}
