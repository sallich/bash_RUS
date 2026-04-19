package ru.bash.syntax.parser

import ru.bash.syntax.ast.ArgumentNode
import ru.bash.syntax.ast.AssignNode
import ru.bash.syntax.ast.AstNode
import ru.bash.syntax.ast.CommandNode
import ru.bash.syntax.ast.PipelineNode
import ru.bash.syntax.ast.ShellWordNode
import ru.bash.syntax.ast.SingleQuotedNode
import ru.bash.syntax.ast.StringNode
import ru.bash.syntax.ast.ArithmeticExpansionNode
import ru.bash.syntax.ast.CommandSubstitutionNode
import ru.bash.syntax.ast.ExitStatusNode
import ru.bash.syntax.ast.VariableNode
import ru.bash.syntax.ast.WordNode
import ru.bash.syntax.errors.ParseException
import ru.bash.syntax.token.Token
import ru.bash.syntax.token.TokenType

class Parser(
    tokens: List<Token>,
    private val source: String,
) {
    private val stream = TokenStream(tokens)

    private companion object {
        private val ARG_TOKEN_TYPES = setOf(
            TokenType.WORD,
            TokenType.VAR,
            TokenType.EXIT_STATUS,
            TokenType.COMMAND_SUBSTITUTION,
            TokenType.ARITHMETIC_EXPANSION,
            TokenType.SINGLE_QUOTED,
            TokenType.DOUBLE_QUOTED_LITERAL,
        )
        private val QUOTED_TYPES = setOf(
            TokenType.SINGLE_QUOTED,
            TokenType.DOUBLE_QUOTED_LITERAL,
        )
        private val ASSIGN_NAME_RE = Regex("[a-zA-Z_][a-zA-Z0-9_]*")
    }

    fun parse(): AstNode {
        val assign = tryParseAssign()
        if (assign != null) {
            if (!stream.end()) throw ParseException("Unexpected token after assignment", stream.peek().pos)
            return assign
        }
        val pipeline = parsePipeline()
        if (!stream.end()) {
            val token = stream.peek()
            throw ParseException("Unexpected token after pipeline", token.pos)
        }
        return pipeline
    }

    private fun tryParseAssign(): AssignNode? {
        val token = stream.peek()
        val eqIdx = token.text.indexOf('=')
        val nameText = token.text.substringBefore('=')
            .takeIf { token.type == TokenType.WORD && eqIdx >= 1 && it.matches(ASSIGN_NAME_RE) }
            ?: return null

        val literalSuffix = token.text.substringAfter('=')
        stream.next()
        val parts = mutableListOf<ArgumentNode>()
        val types = mutableListOf<TokenType>()
        var end = token.endExclusive

        if (literalSuffix.isNotEmpty()) {
            parts += WordNode(literalSuffix)
            types += TokenType.WORD
        }

        while (!stream.end() && isAdjacentToAssignValue(stream.peek(), end, types)) {
            val t = stream.next()
            types += t.type
            parts += argumentNodeFor(t)
            end = t.endExclusive
        }

        val value: ArgumentNode = when {
            parts.isEmpty() -> WordNode("")
            parts.size == 1 -> parts.single()
            else -> ShellWordNode(parts)
        }
        return AssignNode(nameText, value)
    }

    private fun isAdjacentToAssignValue(t: Token, end: Int, types: List<TokenType>): Boolean =
        when {
            t.type !in ARG_TOKEN_TYPES -> false
            t.pos == end -> true
            t.type in QUOTED_TYPES && t.pos == end + 1 -> true
            else -> isConsecutiveDoubleQuoted(t, end, types)
        }

    private fun isConsecutiveDoubleQuoted(t: Token, end: Int, types: List<TokenType>): Boolean {
        if (t.type != TokenType.DOUBLE_QUOTED_LITERAL || types.isEmpty()) return false
        return types.last() == TokenType.DOUBLE_QUOTED_LITERAL &&
            t.pos > end && (end until t.pos).all { source[it] == '"' }
    }

    private fun parsePipeline(): PipelineNode {
        val commands = mutableListOf<CommandNode>()
        commands += parseCommand()
        while (stream.match(TokenType.PIPELINE)) {
            if (stream.end()) throw ParseException("Expected command after '|'", stream.peek().pos)
            commands += parseCommand()
        }
        return PipelineNode(commands)
    }

    private fun parseCommand(): CommandNode {
        val nameNode = parseShellWord()
            ?: throw ParseException("Expected command name", stream.peek().pos)

        val args = mutableListOf<ArgumentNode>()
        while (true) {
            val arg = parseShellWord() ?: break
            args += arg
        }

        return CommandNode(nameNode, args)
    }

    private fun parseShellWord(): ArgumentNode? {
        val parts = mutableListOf<ArgumentNode>()
        val types = mutableListOf<TokenType>()
        var end = 0
        while (!stream.end() && shouldExtendShellWord(stream.peek(), end, types)) {
            val t = stream.next()
            types += t.type
            parts += argumentNodeFor(t)
            end = t.endExclusive
        }
        return normalizeShellWord(parts, types)
    }

    private fun shouldExtendShellWord(t: Token, end: Int, types: List<TokenType>): Boolean =
        when {
            t.type !in ARG_TOKEN_TYPES -> false
            types.isEmpty() || t.pos == end -> true
            else ->
                t.pos > end &&
                    t.type == TokenType.DOUBLE_QUOTED_LITERAL &&
                    types.last() == TokenType.DOUBLE_QUOTED_LITERAL &&
                    (end until t.pos).all { source[it] == '"' }
        }

    private fun normalizeShellWord(
        parts: List<ArgumentNode>,
        types: List<TokenType>,
    ): ArgumentNode? {
        return when {
            parts.isEmpty() -> null
            parts.size == 1 && types.single() == TokenType.DOUBLE_QUOTED_LITERAL ->
                parts.single()
            parts.size == 1 -> parts.single()
            else -> ShellWordNode(parts)
        }
    }

    private fun argumentNodeFor(token: Token): ArgumentNode = when (token.type) {
        TokenType.WORD -> WordNode(token.text)
        TokenType.VAR -> VariableNode(token.text)
        TokenType.EXIT_STATUS -> ExitStatusNode
        TokenType.COMMAND_SUBSTITUTION -> CommandSubstitutionNode(token.text)
        TokenType.ARITHMETIC_EXPANSION -> ArithmeticExpansionNode(token.text)
        TokenType.SINGLE_QUOTED -> SingleQuotedNode(token.text)
        TokenType.DOUBLE_QUOTED_LITERAL -> StringNode(token.text)
        else -> error("Unreachable: ${token.type} not in ARG_TOKEN_TYPES")
    }
}
