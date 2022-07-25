package calculator

import java.math.BigInteger

fun main() {
    var line = readln().trim()
    val variables = mutableMapOf<String, BigInteger>()
    while (line != "/exit") {
        try {
            if (line.startsWith("/")) {
                processCommand(line)
            } else if (line.contains("=")) {
                saveVariable(line, variables)
            } else if (line.isNotEmpty()) {
                println(processExpression(line, variables))
            }
        } catch (e: Exception) {
            println(e.message)
        }
        line = readln().trim()
    }
    println("Bye!")
}

fun processCommand(command: String) {
    when (command) {
        "/help" -> println("It's a calculator witch accept: \"+\", \"-\", \"*\", \"/\", \"(\" and \")\"")
        else -> throw Exception("Unknown command")
    }
}

fun saveVariable(line: String, variables: MutableMap<String, BigInteger>) {
    val (variable, expression) = line.split("=", limit = 2).map(String::trim)
    if (!"[a-zA-Z]*".toRegex().matches(variable)) {
        throw Exception("Invalid identifier")
    }
    variables[variable] = processExpression(expression, variables)
}

fun processExpression(line: String, variables: MutableMap<String, BigInteger>): BigInteger {
    validateExpression(line)
    return getExpressionResult(getNormalizedLine(line), variables)
}

fun validateExpression(line: String) {
    if ("\\w+".toRegex().matches(line) && !"\\d+".toRegex().matches(line) &&
            !"[a-zA-Z]+".toRegex().matches(line)) {
        throw Exception("Invalid identifier")
    }
    if (".*([a-zA-Z]\\d|\\d[a-zA-Z]).*".toRegex().matches(line)) {
        throw Exception("Invalid assigment")
    }
    if (".*[*/]{2,}.*".toRegex().matches(line)){
        throw Exception("Invalid expression")
    }
    if (line.count { it == '(' } != line.count { it == ')' }) {
        throw Exception("Invalid expression")
    }
}

fun getNormalizedLine(line: String): String {
    return line
            .replace("\\s+".toRegex(), "")
            .replace("-{3}".toRegex(), "-")
            .replace("-{2}".toRegex(), "+")
            .replace("\\++".toRegex(), "+")
            .replace("+-", "-")
            .trim()
}

fun getExpressionResult(line: String, variables: MutableMap<String, BigInteger>): BigInteger {
    val operands = mutableListOf<BigInteger>()
    val operators = mutableListOf<Char>()
    var i = 0

    while (i < line.length) {
        if (line[i].isDigit()) {
            i = processDigit(line, i, operands, getSign(operators, operands.size))
        } else if (line[i].isLetter()) {
            i = processVariable(line, i, variables, operands, getSign(operators, operands.size))
        } else {
            processOperator(line[i], operands, operators)
            i++
        }
    }

    for (index in operators.lastIndex downTo 0) {
        count(operands, operators.removeLast())
    }
    return operands.removeFirst()
}

fun processVariable(line: String,
                    start: Int,
                    variables: MutableMap<String, BigInteger>,
                    operands: MutableList<BigInteger>,
                    sign: BigInteger): Int {
    var variableName = ""
    var i = start

    while (i < line.length && line[i].isLetter()) {
        variableName += line[i]
        i++
    }
    if (!variables.containsKey(variableName)) {
        throw Exception("Unknown variable")
    }
    operands.add(sign * variables[variableName]!!)
    return i
}

fun processDigit(line: String, start: Int, operands: MutableList<BigInteger>, sign: BigInteger): Int {
    var value = BigInteger.ZERO
    var i = start

    while (i < line.length && line[i].isDigit()) {
        value = value * 10.toBigInteger() + line[i].digitToInt().toBigInteger()
        i++
    }
    operands.add(sign * value)
    return i
}

fun getSign(operators: MutableList<Char>, operandsSize: Int): BigInteger {
    if (operators.size > 0) {
        if (operators.last() == '-' && operators.count { it !in "()" } > operandsSize) {
            operators.removeLast()
            (-1).toBigInteger()
        }
    }
    return 1.toBigInteger()
}

fun processOperator(operator: Char, operands: MutableList<BigInteger>, operators: MutableList<Char>) {
    if (operator == ')') {
        if (!operators.contains('(')) throw Exception("Invalid expression")
        for (i in operators.lastIndex downTo operators.lastIndexOf('(')) {
            if (operators[i] != '(') {
                count(operands, operators[i])
            }
            operators.removeLast()
        }
    } else if (operator != '(') {
        while (operators.isNotEmpty() && !isHigher(operators.last(), operator)) {
            count(operands, operators.removeLast())
        }
        operators.add(operator)
    } else {
        operators.add(operator)
    }
}

fun count(operands: MutableList<BigInteger>, operator: Char) {
    val last = operands.removeLast()
    val current = operands.last()
    operands[operands.lastIndex] = when (operator) {
        '+' -> current + last
        '-' -> current - last
        '*' -> current * last
        '/' -> current / last
        else -> throw Exception("Invalid expression")
    }
}

fun isHigher(lastOperator: Char?, currentOperator: Char): Boolean {
    if (lastOperator == null || lastOperator == '(') return true
    if (currentOperator in "*/" && lastOperator in "+-") return true

    return false
}
