package ru.bash.syntax.ast

data class SingleQuotedNode(val value: String) : ArgumentNode {
    override fun <R> accept(visitor: AstVisitor<R>):
            R = visitor.visitSingleQuoted(this)
}
