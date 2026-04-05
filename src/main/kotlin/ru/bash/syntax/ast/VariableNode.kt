package ru.bash.syntax.ast

data class VariableNode(val value: String) : ArgumentNode {
    override fun <R> accept(visitor: AstVisitor<R>):
            R = visitor.visitVariable(this)
}
