package ru.bash.syntax.lexer

import org.junit.jupiter.api.Test
import io.kotest.matchers.shouldBe
import ru.bash.syntax.token.TokenType

class LexerUnitTest {
    @Test
    fun `simple command`() {
        val tokens = Lexer("echo hello").tokenize()
        tokens.map {it.type} shouldBe listOf(
            TokenType.WORD,
            TokenType.WORD,
            TokenType.EOF
        )
    }

    @Test
    fun `pipeline test`() {
        val tokens = Lexer("a | b | c").tokenize()
        tokens.map {it.type} shouldBe listOf(
            TokenType.WORD,
            TokenType.PIPELINE,
            TokenType.WORD,
            TokenType.PIPELINE,
            TokenType.WORD,
            TokenType.EOF
        )
    }

    @Test
    fun `string test`() {
        val tokens = Lexer("echo \"hello world\"").tokenize()
        tokens.map {it.type} shouldBe listOf(
            TokenType.WORD,
            TokenType.STRING,
            TokenType.EOF
        )
        tokens[1].text shouldBe "hello world"
    }

    @Test
    fun `single quote test`() {
        val tokens = Lexer("echo 'hello world'").tokenize()
        tokens.map {it.type} shouldBe listOf(
            TokenType.WORD,
            TokenType.SINGLE_QUOTED,
            TokenType.EOF
        )
        tokens[1].text shouldBe "hello world"
    }

    @Test
    fun `variable token`() {
        val tokens = Lexer("echo \$HOME").tokenize()

        tokens.map {it.type} shouldBe listOf(
            TokenType.WORD,
            TokenType.VAR,
            TokenType.EOF
        )
        tokens[1].text shouldBe "HOME"
    }

    @Test
    fun `empty string`() {
        val tokens = Lexer("echo \"\"").tokenize()

        tokens[1].text shouldBe ""
    }
}
