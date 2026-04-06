package ru.bash.semantic

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import ru.bash.syntax.ast.ShellWordNode
import ru.bash.syntax.ast.SingleQuotedNode
import ru.bash.syntax.ast.StringNode
import ru.bash.syntax.ast.VariableNode
import ru.bash.syntax.ast.WordNode

class ShellArgumentExpandVisitorTest {

    private val env = mapOf(
        "A" to "alpha",
        "B" to "beta"
    )

    private val visitor = ShellArgumentExpandVisitor(env)

    @Test
    fun `visit word returns literal`() {
        WordNode("x").accept(visitor) shouldBe "x"
    }

    @Test
    fun `visit string returns content`() {
        StringNode("inside quotes").accept(visitor) shouldBe "inside quotes"
    }

    @Test
    fun `visit single quoted returns literal without expansion`() {
        SingleQuotedNode("\$A").accept(visitor) shouldBe "\$A"
    }

    @Test
    fun `visit variable substitutes from environment`() {
        VariableNode("A").accept(visitor) shouldBe "alpha"
    }

    @Test
    fun `visit undefined variable yields empty string`() {
        VariableNode("UNDEFINED").accept(visitor) shouldBe ""
    }

    @Test
    fun `visit shell word concatenates parts in order`() {
        val node = ShellWordNode(
            listOf(
                WordNode("pre-"),
                VariableNode("A"),
                StringNode("-"),
                VariableNode("B"),
                WordNode("-end")
            )
        )
        node.accept(visitor) shouldBe "pre-alpha-beta-end"
    }

    @Test
    fun `visit nested shell word via recursive accept`() {
        val inner = ShellWordNode(listOf(VariableNode("A"), WordNode("_")))
        val outer = ShellWordNode(listOf(WordNode("<<"), inner, WordNode(">>")))
        outer.accept(visitor) shouldBe "<<alpha_>>"
    }
}
