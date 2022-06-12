package calculator

import java.math.BigInteger
import java.util.*


fun main() {
    val operationRegex = Regex("([-+]*\\w+(\\s*([()\\/+*-])+\\s*)*)*")
    //val operationRegex = Regex("([-+]?\\w+)((\\s*[-+\\*\\/\\(\\)]+)\\s*([-+]?\\w+))*")
    val variableRegex = Regex("(\\s*[-+]?\\w+)((\\s*=+)\\s*([-+]?\\w+)\\s*)*")
    val printVariableRegex = Regex("\\s*\\w+\\s*")
    val variableMap = mutableMapOf<String, BigInteger>()
    while(true) {
        //val numbers = readLine()!!.split(' ').map { it.toInt() }
        val entry = readLine()!!


        when {
            entry == "/exit" -> break
            entry == "" -> continue
            entry == " " -> continue
            entry == "/help" -> printHelpMessage()
            printVariableRegex.matches(entry) -> printVariable(entry, variableMap)
            variableRegex.matches(entry) -> populateVarMap(entry, variableMap)
            operationRegex.matches(entry) -> operate(entry, variableMap)
            !operationRegex.matches(entry) && !"/[a-zA-Z]+".toRegex().matches(entry)-> println("Invalid expression")
            else -> println("Unknown command")
        }
    }
    println("Bye!")

}

private fun formatExpression(s: String): String =
    s.replace("--", "+").replace(Regex("\\++"), "+").replace("+-", "-")

private fun hasPrecedence(op: String, op2: String) = (op in "*/" && op2 !in "*/") || (op in "+-" && op2 == "(")

private fun isNum(string: String) = string.toBigIntegerOrNull() != null
private fun isVarName(name: String) = name.matches(Regex("[a-zA-Z]+"))
private fun convertInfixToPostfix(s: String) : MutableList<String> {
    val stack = mutableListOf<String>()
    val postfix = mutableListOf<String>()

    val infix = formatExpression(s).replace(Regex("\\s*([+*/()]|-(?!\\d))\\s*")," $1 ")
        .trim().split(Regex("\\s+"))

    scanner@ for (op in infix) {
        when {
            // if the scanned character is an operand, append it to the postfix string
            isNum(op) || isVarName(op) -> postfix += op

            // left parentheses are always pushed onto the stack
            op == "(" -> stack += op

            // right parenthesis encountered? pop an operator of the stack and copy to the output, repeat this
            // until the top of the stack is a left parenthesis, then discard both parentheses
            op == ")" -> {
                while (stack.size > 0) {
                    val op2 = stack.removeLast()
                    if (op2 == "(") continue@scanner
                    postfix += op2
                }

                // if stack is empty here, parentheses were unbalanced
                if (stack.size == 0) { postfix.clear(); break@scanner }
            }

            // if a) precedence of the scanned operator is greater than the precedence order of the operator
            // on the stack, or b) the stack is empty or c) the stack contains a ‘(‘ , push it on the stack.
            stack.isEmpty() || stack.last() == "(" || hasPrecedence(op, stack.last()) -> stack += op

            // a) pop all the operators from the stack which are greater than or equal to in precedence than that of
            // the scanned operator. Then b) push the scanned operator to the stack. If we find parentheses while
            // popping, then stop and push the scanned operator on the stack.
            else -> {
                while (stack.size > 0) {
                    if (hasPrecedence(op, stack.last())) break
                    val op2 = stack.removeLast()
                    if (op2 == "(") break
                    postfix += op2
                }
                stack += op
            }
        }
    }

    // at the end of the expression, pop the stack and add all operators to the result.
    while (stack.size > 0) {
        val op2 = stack.removeLast()
        // if we encounter a left parenthesis here, parentheses were unbalanced
        if (op2 == "(") { postfix.clear(); break }
        postfix += op2
    }

    return postfix
}

fun printVariable(entry: String, map: MutableMap<String, BigInteger>) {
    println(map[entry.replace(Regex("\\s"), "")] ?: "Unknown variable")
}

fun printHelpMessage() = println("The program calculates the sum of numbers")

fun populateVarMap(entry: String, varMap: MutableMap<String, BigInteger>) {
    val entries = entry.split(Regex("(\\s*=\\s*)")).map { it.replace(" ","") }.toMutableList()
    val validIdentifierRegex = Regex("[a-zA-Z]+")
    //val validAssigmentRegex = Regex("\\d+ | [a-zA-Z]+")


    if (validIdentifierRegex.matches(entries.last())) {
        val storedValue = varMap[entries.last()]
        if (storedValue == null) {
            println("Unknown variable")
            return
        } else {
            varMap[entries.first()] = storedValue
            return
        }
    }

    if (!validIdentifierRegex.matches(entries.first())) {
        println("Invalid identifier")
        return
    }
    if (entries.size > 2) {
        println("Invalid assignment")
        return
    }

    try {
        varMap[entries.first()] = entries.last().toBigInteger()
    } catch (e: java.lang.NumberFormatException) {
        println("Invalid assignment")
    }
    return
}

fun isOperator(value: String): Boolean {
    return value == "+" || value == "-" || value == "*" || value == "/"
}

fun expresionBalanced(entry: String): Boolean {
    val open = entry.count { it == '(' }
    val closed = entry.count { it == ')' }
    val stars = Regex(".*(\\*\\*)+.*")
    val slash = Regex(".*(\\/\\/)+.*")

    return open == closed && !stars.matches(entry) && !slash.matches(entry)
}

fun operate(entry: String, variableMap: MutableMap<String, BigInteger>) {

    if (!expresionBalanced(entry)) {
        println("Invalid Expression")
        return
    }

    val postfixList = convertInfixToPostfix(entry)

    val stack = Stack<BigInteger>()

    postfixList.forEach {
        if (isOperator(it)) {
            stack.push(getOperation(it)(stack.pop(), stack.pop()))
        } else {
            stack.push(
                when (it) {
                    in variableMap -> variableMap[it]
                    else -> it.toBigInteger()
                }
            )
        }
    }

    println(stack.pop())
}

fun getOperation(operation: String): (BigInteger, BigInteger)-> BigInteger {
    val minusRegex = Regex("-(--)*")
    val multiplyRegex = Regex("\\*")
    val divideRegex = Regex("\\/")

    return when{
        minusRegex.matches(operation) -> ::subtract
        multiplyRegex.matches(operation) -> ::multiply
        divideRegex.matches(operation) -> ::divide
        else -> ::sum
    }
}

fun sum(a: BigInteger, b: BigInteger): BigInteger {
    return a + b
}

fun subtract(a: BigInteger, b:BigInteger): BigInteger {
    return b - a
}

fun multiply(a: BigInteger, b:BigInteger): BigInteger {
    return a * b
}

fun divide(a: BigInteger, b: BigInteger): BigInteger = b / a

fun String.isNumber(): Boolean {
    try {
        this.toInt()
        return true
    } catch (e: Exception) {
        return false
    }
}
