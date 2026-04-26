package ru.bash.semantic

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ru.bash.syntax.ast.ArithmeticExpansionNode
import ru.bash.syntax.ast.CommandSubstitutionNode
import ru.bash.syntax.ast.ExitStatusNode
import ru.bash.syntax.ast.ShellWordNode
import ru.bash.syntax.ast.SingleQuotedNode
import ru.bash.syntax.ast.StringNode
import ru.bash.syntax.ast.VariableNode
import ru.bash.syntax.ast.WordNode

class ShellWordExpanderTest {

    private val env = mapOf(
        "A" to "alpha",
        "B" to "beta",
        "X" to "5"
    )

    private val noSubst = CommandSubstitutionRunner {
        throw UnsupportedOperationException("not used")
    }

    private fun expander(lastExit: Int = 0) =
        ShellWordExpander(ExpansionContext(env, lastExit), noSubst)

    private suspend fun expand(node: ru.bash.syntax.ast.ArgumentNode) = expander().expand(node)

    @Test
    fun `visit word returns literal`(): Unit = runBlocking {
        expand(WordNode("x")) shouldBe "x"
    }

    @Test
    fun `visit string returns content`(): Unit = runBlocking {
        expand(StringNode("inside quotes")) shouldBe "inside quotes"
    }

    @Test
    fun `visit single quoted returns literal without expansion`(): Unit = runBlocking {
        expand(SingleQuotedNode("\$A")) shouldBe "\$A"
    }

    @Test
    fun `visit variable substitutes from environment`(): Unit = runBlocking {
        expand(VariableNode("A")) shouldBe "alpha"
    }

    @Test
    fun `visit undefined variable yields empty string`(): Unit = runBlocking {
        expand(VariableNode("UNDEFINED")) shouldBe ""
    }

    @Test
    fun `visit shell word concatenates parts in order`(): Unit = runBlocking {
        val node = ShellWordNode(
            listOf(
                WordNode("pre-"),
                VariableNode("A"),
                StringNode("-"),
                VariableNode("B"),
                WordNode("-end")
            )
        )
        expand(node) shouldBe "pre-alpha-beta-end"
    }

    @Test
    fun `visit nested shell word via recursive expand`(): Unit = runBlocking {
        val inner = ShellWordNode(listOf(VariableNode("A"), WordNode("_")))
        val outer = ShellWordNode(listOf(WordNode("<<"), inner, WordNode(">>")))
        expand(outer) shouldBe "<<alpha_>>"
    }

    @Test
    fun `exit status expands to last exit code`(): Unit = runBlocking {
        expander(lastExit = 42).expand(ExitStatusNode) shouldBe "42"
    }

    @Test
    fun `arithmetic expansion`(): Unit = runBlocking {
        expander().expand(ArithmeticExpansionNode("1 + 2 * 3")) shouldBe "7"
    }

    @Test
    fun `arithmetic expansion supports bare variable names`(): Unit = runBlocking {
        expander().expand(ArithmeticExpansionNode("X + 1")) shouldBe "6"
    }

    @Test
    fun `arithmetic expansion supports dollar variables`(): Unit = runBlocking {
        expander().expand(ArithmeticExpansionNode("\$X + 2")) shouldBe "7"
    }

    @Test
    fun `command substitution delegates to runner`(): Unit = runBlocking {
        val e = ShellWordExpander(
            ExpansionContext(env, 0)
        ) { inner -> "[$inner]" }
        e.expand(CommandSubstitutionNode("echo hi")) shouldBe "[echo hi]"
    }
}
