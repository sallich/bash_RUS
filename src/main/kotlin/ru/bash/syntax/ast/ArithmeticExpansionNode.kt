package ru.bash.syntax.ast

data class ArithmeticExpansionNode(val expression: String) : ArgumentNode {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitArithmeticExpansion(this)
}
