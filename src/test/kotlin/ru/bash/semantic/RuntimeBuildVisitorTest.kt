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

    @Test
    fun `expand variables and merge shell words`() {
        val ast = Parser(Lexer("echo hello\$USER").tokenize()).parse()
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
        val ast = Parser(Lexer("echo '\$HOME'").tokenize()).parse()
        val exec = RuntimeBuildVisitor(mapOf("HOME" to "/tmp")).build(ast)
        exec.commands.single().argv shouldBe listOf("echo", "\$HOME")
    }

    @Test
    fun `double quoted content expanded as string token`() {
        val ast = Parser(Lexer("echo \"hi\"").tokenize()).parse()
        val exec = RuntimeBuildVisitor(emptyMap()).build(ast)
        exec.commands.single().argv shouldBe listOf("echo", "hi")
    }

    @Test
    fun `pipeline builds multiple commands`() {
        val ast = Parser(Lexer("a | b").tokenize()).parse()
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
        val ast = Parser(Lexer("one | two | three | four").tokenize()).parse()
        val exec = RuntimeBuildVisitor(emptyMap()).build(ast)
        exec.commands.map { it.name } shouldBe listOf("one", "two", "three", "four")
    }

    @Test
    fun `undefined variable expands to empty in glued word`() {
        val ast = Parser(Lexer("echo x\$MISSING y").tokenize()).parse()
        val exec = RuntimeBuildVisitor(emptyMap()).build(ast)
        exec.commands.single().argv shouldBe listOf("echo", "x", "y")
    }

    @Test
    fun `multiple argv from spaced words and one glued`() {
        val ast = Parser(Lexer("cmd a b\$X c").tokenize()).parse()
        val exec = RuntimeBuildVisitor(mapOf("X" to "2")).build(ast)
        exec.commands.single().argv shouldBe listOf("cmd", "a", "b2", "c")
    }

    @Test
    fun `build clears state between invocations`() {
        val visitor = RuntimeBuildVisitor(emptyMap())
        visitor.build(Parser(Lexer("a").tokenize()).parse())
        val second = visitor.build(Parser(Lexer("b | c").tokenize()).parse())
        second.commands.size shouldBe 2
    }

    @Test
    fun `glued quoted and word expands as one argument`() {
        val ast = Parser(Lexer("echo \"z\"tail").tokenize()).parse()
        val exec = RuntimeBuildVisitor(emptyMap()).build(ast)
        exec.commands.single().argv shouldBe listOf("echo", "ztail")
    }
}
