package ru.bash.syntax.ast

data class StringNode(val value: String) : ArgumentNode {
    override fun <R> accept(visitor: AstVisitor<R>):
            R = visitor.visitString(this)
}
