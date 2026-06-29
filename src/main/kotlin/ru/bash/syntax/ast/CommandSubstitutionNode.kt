package ru.bash.syntax.ast

data class CommandSubstitutionNode(val inner: String) : ArgumentNode {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitCommandSubstitution(this)
}
