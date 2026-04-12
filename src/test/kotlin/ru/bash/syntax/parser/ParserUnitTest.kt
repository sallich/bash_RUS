package ru.bash.syntax.parser

import org.junit.jupiter.api.Test
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertThrows
import ru.bash.syntax.ast.AssignNode
import ru.bash.syntax.ast.PipelineNode
import ru.bash.syntax.ast.ShellWordNode
import ru.bash.syntax.ast.StringNode
import ru.bash.syntax.ast.VariableNode
import ru.bash.syntax.ast.WordNode
import ru.bash.syntax.errors.ParseException
import ru.bash.syntax.lexer.Lexer

class ParserUnitTest {

    private fun parse(line: String) = Parser(Lexer(line).tokenize(), line).parse() as PipelineNode
    private fun parseAst(line: String) = Parser(Lexer(line).tokenize(), line).parse()

    @Test
    fun `parse simple command`() {
        val line = "echo hello"
        val ast = Parser(Lexer(line).tokenize(), line).parse() as PipelineNode
        ast.nodes.size shouldBe 1
        ast.nodes[0].name shouldBe "echo"
    }

    @Test
    fun `parse simple assignment`() {
        val ast = parseAst("FOO=bar")
        ast shouldBe AssignNode("FOO", WordNode("bar"))
    }

    @Test
    fun `parse empty assignment`() {
        val ast = parseAst("FOO=")
        ast shouldBe AssignNode("FOO", WordNode(""))
    }

    @Test
    fun `parse assignment with variable value`() {
        val ast = parseAst("X=\$HOME")
        ast shouldBe AssignNode("X", VariableNode("HOME"))
    }

    @Test
    fun `parse assignment with glued word and variable`() {
        val ast = parseAst("PATH=/usr/bin:\$HOME")
        ast shouldBe AssignNode("PATH", ShellWordNode(listOf(WordNode("/usr/bin:"), VariableNode("HOME"))))
    }

    @Test
    fun `parse arguments`() {
        val ast = parse("echo hello world")
        val args = ast.nodes[0].nodes
        args.size shouldBe 2
    }

    @Test
    fun `parse string argument`() {
        val ast = parse("echo \"hello\"")
        val arg = ast.nodes[0].nodes[0]
        arg shouldBe StringNode("hello")
    }

    @Test
    fun `parse variable`() {
        val ast = parse("echo \$HOME")
        val arg = ast.nodes[0].nodes[0]
        arg shouldBe VariableNode("HOME")
    }

    @Test
    fun `parse double quoted variable as shell word`() {
        val ast = parse("echo \"prefix\$HOME\"")
        val arg = ast.nodes[0].nodes[0]
        arg shouldBe ShellWordNode(listOf(StringNode("prefix"), VariableNode("HOME")))
    }

    @Test
    fun `parse concatenated unquoted word and variable`() {
        val ast = parse("echo dir\$HOME")
        val arg = ast.nodes[0].nodes[0]
        arg shouldBe ShellWordNode(listOf(WordNode("dir"), VariableNode("HOME")))
    }

    @Test
    fun `parse braced variable`() {
        val ast = parse("echo \${HOME}")
        val arg = ast.nodes[0].nodes[0]
        arg shouldBe VariableNode("HOME")
    }

    @Test
    fun `parse adjacent double quoted segments as one shell word`() {
        val ast = parse("echo \"a\"\"b\"")
        val arg = ast.nodes[0].nodes[0]
        arg shouldBe ShellWordNode(listOf(StringNode("a"), StringNode("b")))
    }

    @Test
    fun `pipe without command`() {
        assertThrows<ParseException> {
            parse("echo |")
        }
    }

    @Test
    fun `pipe at start`() {
        assertThrows<ParseException> {
            parse("| cat")
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
            val ast = parse(input)
            ast.nodes.map { it.name } shouldBe expected
        }
    }
}
