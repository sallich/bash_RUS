package ru.bash.syntax.ast

@Suppress("TooManyFunctions")
interface AstVisitor<out R> {
    fun visitPipeline(node: PipelineNode): R
    fun visitCommand(node: CommandNode): R
    fun visitAssign(node: AssignNode): R
    fun visitShellWord(node: ShellWordNode): R
    fun visitWord(node: WordNode): R
    fun visitString(node: StringNode): R
    fun visitSingleQuoted(node: SingleQuotedNode): R
    fun visitVariable(node: VariableNode): R
    fun visitExitStatus(node: ExitStatusNode): R
    fun visitCommandSubstitution(node: CommandSubstitutionNode): R
    fun visitArithmeticExpansion(node: ArithmeticExpansionNode): R
}
