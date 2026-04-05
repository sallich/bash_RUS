package ru.bash.syntax.parser

import ru.bash.syntax.ast.ArgumentNode
import ru.bash.syntax.ast.CommandNode
import ru.bash.syntax.ast.PipelineNode
import ru.bash.syntax.ast.SingleQuotedNode
import ru.bash.syntax.ast.StringNode
import ru.bash.syntax.ast.VariableNode
import ru.bash.syntax.ast.WordNode
import ru.bash.syntax.errors.ParseException
import ru.bash.syntax.token.Token
import ru.bash.syntax.token.TokenType

class Parser(tokens: List<Token>) {
    private val stream = TokenStream(tokens)

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
            val arg = parseArgument() ?: break
            args += arg
        }
        return CommandNode(nameToken.text, args)
    }

    private fun parseArgument() : ArgumentNode? {
        val token = stream.peek()
        return when(token.type) {
            TokenType.WORD -> {
                stream.next()
                WordNode(token.text)
            }
            TokenType.VAR -> {
                stream.next()
                VariableNode(token.text)
            }
            TokenType.SINGLE_QUOTED -> {
                stream.next()
                SingleQuotedNode(token.text)
            }
            TokenType.STRING -> {
                stream.next()
                StringNode(token.text)
            }
            else -> null
        }
    }
}
