package ru.bash.syntax.parser

import org.junit.jupiter.api.Test
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
import ru.bash.syntax.ast.StringNode
import ru.bash.syntax.ast.VariableNode
import ru.bash.syntax.errors.ParseException
import ru.bash.syntax.lexer.Lexer

class ParserUnitTest {

    @Test
    fun `parse simple command`() {
        val tokens = Lexer("echo hello").tokenize()
        val ast = Parser(tokens).parse()
        ast.nodes.size shouldBe 1
        ast.nodes[0].name shouldBe "echo"
    }

    @Test
    fun `parse arguments`() {
        val ast = Parser(Lexer("echo hello world").tokenize()).parse()
        val args = ast.nodes[0].nodes
        args.size shouldBe 2
    }

    @Test
    fun `parse string argument`() {
        val ast = Parser(Lexer("echo \"hello\"").tokenize()).parse()
        val arg = ast.nodes[0].nodes[0]
        arg shouldBe StringNode("hello")
    }

    @Test
    fun `parse variable`() {
        val ast = Parser(Lexer("echo \$HOME").tokenize()).parse()
        val arg = ast.nodes[0].nodes[0]
        arg shouldBe VariableNode("HOME")
    }

    @Test
    fun `pipe without command`() {
        assertThrows<ParseException> {
            Parser(Lexer("echo |").tokenize()).parse()
        }
    }

    @Test
    fun `pipe at start`() {
        assertThrows<ParseException> {
            Parser(Lexer("| cat").tokenize()).parse()
        }
    }

    @Test
    fun `pipeline parsing cases`() {
        val cases = listOf(
            "a" to listOf("a"),
            "a | b" to listOf("a", "b"),
            "a | b | c" to listOf("a", "b", "c")
        )
        for ((input, expected) in cases) {
            val ast = Parser(Lexer(input).tokenize()).parse()
            ast.nodes.map { it.name } shouldBe expected
        }
    }
}