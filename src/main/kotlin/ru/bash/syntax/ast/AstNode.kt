package ru.bash.syntax.ast

sealed interface AstNode {
    fun <R> accept(visitor: AstVisitor<R>): R
}
