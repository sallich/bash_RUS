package ru.bash.semantic

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ru.bash.semantic.model.ExecCommand
import ru.bash.semantic.model.ExecPipeline
import ru.bash.syntax.ast.PipelineNode
import ru.bash.syntax.ast.ShellWordNode
import ru.bash.syntax.ast.VariableNode
import ru.bash.syntax.ast.WordNode
import ru.bash.syntax.lexer.Lexer
import ru.bash.syntax.parser.Parser

class RuntimeBuildVisitorTest {

    private val noSubst = CommandSubstitutionRunner {
        throw UnsupportedOperationException("Command substitution not used in this test")
    }

    private fun expander(env: Map<String, String>, lastExit: Int = 0) =
        ShellWordExpander(ExpansionContext(env, lastExit), noSubst)

    private fun parse(line: String) = Parser(Lexer(line).tokenize(), line).parse() as PipelineNode

    private fun build(env: Map<String, String>, ast: PipelineNode) = runBlocking {
        RuntimeBuildVisitor(expander(env)).build(ast)
    }

    @Test
    fun `expand variables and merge shell words`() {
        val ast = parse("echo hello\$USER")
        val arg = ast.nodes[0].nodes[0]
        arg shouldBe ShellWordNode(listOf(WordNode("hello"), VariableNode("USER")))

        val env = mapOf("USER" to "world")
        val exec = build(env, ast)
        exec shouldBe ExecPipeline(
            listOf(
                ExecCommand(listOf("echo", "helloworld"))
            )
        )
    }

    @Test
    fun `single quoted is literal`() {
        val ast = parse("echo '\$HOME'")
        val exec = build(mapOf("HOME" to "/tmp"), ast)
        exec.commands.single().argv shouldBe listOf("echo", "\$HOME")
    }

    @Test
    fun `double quoted content expanded as string token`() {
        val ast = parse("echo \"hi\"")
        val exec = build(emptyMap(), ast)
        exec.commands.single().argv shouldBe listOf("echo", "hi")
    }

    @Test
    fun `pipeline builds multiple commands`() {
        val ast = parse("a | b")
        val exec = build(emptyMap(), ast)
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
        val exec = build(emptyMap(), ast)
        exec.commands.map { it.name } shouldBe listOf("one", "two", "three", "four")
    }

    @Test
    fun `undefined variable expands to empty in glued word`() {
        val ast = parse("echo x\$MISSING y")
        val exec = build(emptyMap(), ast)
        exec.commands.single().argv shouldBe listOf("echo", "x", "y")
    }

    @Test
    fun `multiple argv from spaced words and one glued`() {
        val ast = parse("cmd a b\$X c")
        val exec = build(mapOf("X" to "2"), ast)
        exec.commands.single().argv shouldBe listOf("cmd", "a", "b2", "c")
    }

    @Test
    fun `build clears state between invocations`(): Unit = runBlocking {
        RuntimeBuildVisitor(expander(emptyMap())).build(parse("a"))
        val second = RuntimeBuildVisitor(expander(emptyMap())).build(parse("b | c"))
        second.commands.size shouldBe 2
    }

    @Test
    fun `glued quoted and word expands as one argument`() {
        val ast = parse("echo \"z\"tail")
        val exec = build(emptyMap(), ast)
        exec.commands.single().argv shouldBe listOf("echo", "ztail")
    }
}
