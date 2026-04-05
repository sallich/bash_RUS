package ru.bash.semantic

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import ru.bash.semantic.model.ExecCommand
import ru.bash.semantic.model.ExecPipeline
import ru.bash.syntax.ast.ShellWordNode
import ru.bash.syntax.ast.VariableNode
import ru.bash.syntax.ast.WordNode
import ru.bash.syntax.lexer.Lexer
import ru.bash.syntax.parser.Parser

class RuntimeBuildVisitorTest {

    private fun parse(line: String) = Parser(Lexer(line).tokenize(), line).parse()

    @Test
    fun `expand variables and merge shell words`() {
        val ast = parse("echo hello\$USER")
        val arg = ast.nodes[0].nodes[0]
        arg shouldBe ShellWordNode(listOf(WordNode("hello"), VariableNode("USER")))

        val env = mapOf("USER" to "world")
        val exec = RuntimeBuildVisitor(env).build(ast)
        exec shouldBe ExecPipeline(
            listOf(
                ExecCommand(listOf("echo", "helloworld"))
            )
        )
    }

    @Test
    fun `single quoted is literal`() {
        val ast = parse("echo '\$HOME'")
        val exec = RuntimeBuildVisitor(mapOf("HOME" to "/tmp")).build(ast)
        exec.commands.single().argv shouldBe listOf("echo", "\$HOME")
    }

    @Test
    fun `double quoted content expanded as string token`() {
        val ast = parse("echo \"hi\"")
        val exec = RuntimeBuildVisitor(emptyMap()).build(ast)
        exec.commands.single().argv shouldBe listOf("echo", "hi")
    }

    @Test
    fun `pipeline builds multiple commands`() {
        val ast = parse("a | b")
        val exec = RuntimeBuildVisitor(emptyMap()).build(ast)
        exec shouldBe ExecPipeline(
            listOf(
                ExecCommand(listOf("a")),
                ExecCommand(listOf("b"))
            )
        )
    }

    @Test
    fun `long pipeline preserves command order`() {
        val ast = parse("one | two | three | four")
        val exec = RuntimeBuildVisitor(emptyMap()).build(ast)
        exec.commands.map { it.name } shouldBe listOf("one", "two", "three", "four")
    }

    @Test
    fun `undefined variable expands to empty in glued word`() {
        val ast = parse("echo x\$MISSING y")
        val exec = RuntimeBuildVisitor(emptyMap()).build(ast)
        exec.commands.single().argv shouldBe listOf("echo", "x", "y")
    }

    @Test
    fun `multiple argv from spaced words and one glued`() {
        val ast = parse("cmd a b\$X c")
        val exec = RuntimeBuildVisitor(mapOf("X" to "2")).build(ast)
        exec.commands.single().argv shouldBe listOf("cmd", "a", "b2", "c")
    }

    @Test
    fun `build clears state between invocations`() {
        val visitor = RuntimeBuildVisitor(emptyMap())
        visitor.build(parse("a"))
        val second = visitor.build(parse("b | c"))
        second.commands.size shouldBe 2
    }

    @Test
    fun `glued quoted and word expands as one argument`() {
        val ast = parse("echo \"z\"tail")
        val exec = RuntimeBuildVisitor(emptyMap()).build(ast)
        exec.commands.single().argv shouldBe listOf("echo", "ztail")
    }
}
