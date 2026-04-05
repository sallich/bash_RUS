package ru.bash.syntax.ast

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.bash.syntax.lexer.Lexer
import ru.bash.syntax.parser.Parser

class AstTraversalTest {

    private fun parse(input: String): PipelineNode =
        Parser(Lexer(input).tokenize(), input).parse()

    private fun walk(ast: PipelineNode): List<String> {
        val visitor = RecordingAstVisitor()
        ast.accept(visitor)
        return visitor.snapshot()
    }

    @Test
    fun `traverse single command with plain words`() {
        val ast = parse("echo hello world")
        walk(ast) shouldBe listOf(
            "Pipeline",
            "Command:echo",
            "Word:hello",
            "Word:world"
        )
    }

    @Test
    fun `traverse pipeline dispatches commands in order`() {
        val ast = parse("grep x | wc -l | cat")
        walk(ast) shouldBe listOf(
            "Pipeline",
            "Command:grep",
            "Word:x",
            "Command:wc",
            "Word:-l",
            "Command:cat"
        )
    }

    @Test
    fun `traverse shell word with glued word and variable`() {
        val ast = parse("echo hello\$USER")
        walk(ast) shouldBe listOf(
            "Pipeline",
            "Command:echo",
            "ShellWord:2",
            "Word:hello",
            "Var:USER"
        )
    }

    @Test
    fun `traverse multiple glued variables in one shell word`() {
        val ast = parse("echo \$a\$b\$c")
        walk(ast) shouldBe listOf(
            "Pipeline",
            "Command:echo",
            "ShellWord:3",
            "Var:a",
            "Var:b",
            "Var:c"
        )
    }

    @Test
    fun `traverse double quoted and single quoted arguments`() {
        val ast = parse("echo \"double here\" 'single here'")
        walk(ast) shouldBe listOf(
            "Pipeline",
            "Command:echo",
            "String:double here",
            "SingleQuoted:single here"
        )
    }

    @Test
    fun `traverse glued string and word in one shell word`() {
        val ast = parse("echo \"pre\"suf")
        walk(ast) shouldBe listOf(
            "Pipeline",
            "Command:echo",
            "ShellWord:2",
            "String:pre",
            "Word:suf"
        )
    }

    @Test
    fun `traverse command without arguments`() {
        val ast = parse("pwd")
        walk(ast) shouldBe listOf(
            "Pipeline",
            "Command:pwd"
        )
    }

    @Test
    fun `traverse variable as sole argument`() {
        val ast = parse("echo \$PATH")
        walk(ast) shouldBe listOf(
            "Pipeline",
            "Command:echo",
            "Var:PATH"
        )
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("mixedShellWordCases")
    fun `traverse mixed glued parts`(input: String, expectedTail: List<String>) {
        val ast = parse(input)
        val full = listOf("Pipeline", "Command:echo") + expectedTail
        walk(ast) shouldBe full
    }

    companion object {
        @JvmStatic
        fun mixedShellWordCases(): List<Array<Any>> = listOf(
            arrayOf(
                "echo 'y'x",
                listOf(
                    "ShellWord:2",
                    "SingleQuoted:y",
                    "Word:x"
                )
            ),
            arrayOf(
                "echo a\$VAR",
                listOf(
                    "ShellWord:2",
                    "Word:a",
                    "Var:VAR"
                )
            )
        )
    }
}

private class RecordingAstVisitor : AstVisitor<Unit> {

    val events = mutableListOf<String>()

    fun snapshot(): List<String> = events.toList()

    override fun visitPipeline(node: PipelineNode) {
        events += "Pipeline"
        node.nodes.forEach { it.accept(this) }
    }

    override fun visitCommand(node: CommandNode) {
        events += "Command:${node.name}"
        node.nodes.forEach { it.accept(this) }
    }

    override fun visitShellWord(node: ShellWordNode) {
        events += "ShellWord:${node.parts.size}"
        node.parts.forEach { it.accept(this) }
    }

    override fun visitWord(node: WordNode) {
        events += "Word:${node.value}"
    }

    override fun visitString(node: StringNode) {
        events += "String:${node.value}"
    }

    override fun visitSingleQuoted(node: SingleQuotedNode) {
        events += "SingleQuoted:${node.value}"
    }

    override fun visitVariable(node: VariableNode) {
        events += "Var:${node.value}"
    }
}
