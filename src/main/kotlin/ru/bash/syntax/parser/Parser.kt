package ru.bash.syntax.parser

import ru.bash.syntax.ast.ArgumentNode
import ru.bash.syntax.ast.CommandNode
import ru.bash.syntax.ast.PipelineNode
import ru.bash.syntax.ast.ShellWordNode
import ru.bash.syntax.ast.SingleQuotedNode
import ru.bash.syntax.ast.StringNode
import ru.bash.syntax.ast.VariableNode
import ru.bash.syntax.ast.WordNode
import ru.bash.syntax.errors.ParseException
import ru.bash.syntax.token.Token
import ru.bash.syntax.token.TokenType

class Parser(tokens: List<Token>) {
    private val stream = TokenStream(tokens)

    private companion object {
        private val ARG_TOKEN_TYPES = setOf(
            TokenType.WORD,
            TokenType.VAR,
            TokenType.SINGLE_QUOTED,
            TokenType.STRING
        )
    }

    fun parse() : PipelineNode {
        val pipeline = parsePipeline()
        if(!stream.end()) {
            val token = stream.peek()
            throw ParseException("Unexpected token after pipeline", token.pos)
        }
        return pipeline
    }

    private fun parsePipeline() : PipelineNode {
        val commands = mutableListOf<CommandNode>()
        commands += parseCommand()
        while (stream.match(TokenType.PIPELINE)) {
            if(stream.end()) throw ParseException("Expected command after '|'", stream.peek().pos)
            commands += parseCommand()
        }
        return PipelineNode(commands)
    }

    private fun parseCommand(): CommandNode {
        val nameToken = stream.expect(
            TokenType.WORD,
            "Expected command name"
        )
        val args = mutableListOf<ArgumentNode>()
        while (true) {
            val arg = parseShellWord() ?: break
            args += arg
        }
        return CommandNode(nameToken.text, args)
    }

    private fun parseShellWord(): ArgumentNode? {
        val parts = mutableListOf<ArgumentNode>()
        var end = 0
        while (!stream.end()) {
            val t = stream.peek()
            if (t.type !in ARG_TOKEN_TYPES || (parts.isNotEmpty() && t.pos != end)) break
            stream.next()
            parts += argumentNodeFor(t)
            end = tokenEndExclusive(t)
        }
        return when {
            parts.isEmpty() -> null
            parts.size == 1 -> parts.single()
            else -> ShellWordNode(parts)
        }
    }

    private fun tokenEndExclusive(t: Token): Int = when (t.type) {
        TokenType.VAR -> t.pos + 1 + t.text.length
        TokenType.SINGLE_QUOTED,
        TokenType.STRING -> t.pos + t.text.length + 1
        else -> t.pos + t.text.length
    }

    private fun argumentNodeFor(token: Token): ArgumentNode = when (token.type) {
        TokenType.WORD -> WordNode(token.text)
        TokenType.VAR -> VariableNode(token.text)
        TokenType.SINGLE_QUOTED -> SingleQuotedNode(token.text)
        TokenType.STRING -> StringNode(token.text)
        else -> error("Unreachable: ${token.type} not in ARG_TOKEN_TYPES")
    }
}
