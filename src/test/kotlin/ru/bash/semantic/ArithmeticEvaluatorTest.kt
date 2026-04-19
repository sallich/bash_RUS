package ru.bash.semantic

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.bash.syntax.errors.ParseException

class ArithmeticEvaluatorTest {

    @Test
    fun `addition and multiplication precedence`() {
        ArithmeticEvaluator.eval("1 + 2 * 3") shouldBe 7L
    }

    @Test
    fun parentheses() {
        ArithmeticEvaluator.eval("(1 + 2) * 3") shouldBe 9L
    }

    @Test
    fun `unary minus`() {
        ArithmeticEvaluator.eval("-5 + 2") shouldBe -3L
    }

    @Test
    fun modulo() {
        ArithmeticEvaluator.eval("7 % 3") shouldBe 1L
    }

    @Test
    fun division() {
        ArithmeticEvaluator.eval("10 / 3") shouldBe 3L
    }

    @Test
    fun `empty expression is zero`() {
        ArithmeticEvaluator.eval("   ") shouldBe 0L
    }

    @Test
    fun `division by zero throws`() {
        assertThrows<ParseException> {
            ArithmeticEvaluator.eval("1 / 0")
        }
    }
}
