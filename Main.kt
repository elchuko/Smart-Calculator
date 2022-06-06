package calculator

import java.math.BigInteger
import java.math.BigDecimal
import java.util.*

fun main() {
    val operationRegex = Regex("([-+]?\\w+)((\\s*[-+]+)\\s*([-+]?\\w+))*")
    val variableRegex = Regex("([-+]?\\w+)((\\s*=+)\\s*([-+]?\\w+)\\s*)*")
    val printVariableRegex = Regex("\\w+")
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

fun printVariable(entry: String, map: MutableMap<String, BigInteger>) {
    println(map[entry] ?: "Unknown variable")
}

fun printHelpMessage() = println("The program calculates the sum of numbers")

fun createStack(entry: String, varMap: MutableMap<String, BigInteger>): Pair<Queue<String>, Queue<BigInteger>> {
    var numberStack: Queue<BigInteger> = LinkedList()
    var operationStack: Queue<String> = LinkedList()
    var numbers = entry.split(Regex("\\s+\\+*-*\\s*")).map {
        try {
            it.toBigInteger()
        } catch (e: java.lang.NumberFormatException) {
            varMap[it]
        }
    }.toMutableList()
    var operations = entry.split(Regex("(\\w+\\s)|(\\s\\w*\\s)|(\\s\\w+)")).map { it.replace(Regex("\\++"), "+") }.toMutableList()
    operations.removeAll(listOf("", " "))

    numberStack.addAll(numbers)
    operationStack.addAll(operations)

    return (operationStack to numberStack)
}

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


fun operate(entry: String, variableMap: MutableMap<String, BigInteger>) {
    var regex = Regex("([-+]?\\w+)((\\s*[-+]+)\\s*([-+]?\\w+))*")

    var (operationStack, numberStack) = createStack(entry, variableMap)
    var current: BigInteger
    try {
        current = numberStack.poll()
    } catch (e: Exception) {
        current = BigInteger.ZERO
    }


    while(!operationStack.isEmpty() && !numberStack.isEmpty()) {
        var a = numberStack.poll()
        if (a == null) break
        current = getOperation(operationStack.poll())(current, a)
    }

    println(current)
}

fun getOperation(operation: String): (BigInteger, BigInteger)-> BigInteger {
    var operationRegex = Regex("-(--)*")
    return when{
        operationRegex.matches(operation) -> ::subtract
        else -> ::sum
    }
}

fun sum(a: BigInteger, b: BigInteger): BigInteger {
    return a + b
}

fun subtract(a: BigInteger, b:BigInteger): BigInteger {
    return a - b
}

fun multiply(a: BigInteger, b:BigInteger): BigInteger {
    return a * b
}

fun String.isNumber(): Boolean {
    try {
        this.toInt()
        return true
    } catch (e: Exception) {
        return false
    }
}
