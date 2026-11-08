package com.kzos

/** Small arithmetic parser; intentionally avoids evaluating arbitrary code. */
object CalculatorEngine {
    fun evaluate(source: String): Double {
        val parser = Parser(source)
        val value = parser.expression()
        require(parser.done()) { "Unexpected input" }
        return value
    }

    private class Parser(private val input: String) {
        private var position = 0
        fun done(): Boolean { skipSpaces(); return position == input.length }
        fun expression(): Double {
            var value = term()
            while (true) {
                skipSpaces()
                value = when {
                    take('+') -> value + term()
                    take('-') -> value - term()
                    else -> return value
                }
            }
        }
        private fun term(): Double {
            var value = factor()
            while (true) {
                skipSpaces()
                value = when {
                    take('*') -> value * factor()
                    take('/') -> value / factor()
                    else -> return value
                }
            }
        }
        private fun factor(): Double {
            skipSpaces()
            if (take('-')) return -factor()
            if (take('+')) return factor()
            if (take('(')) {
                val value = expression()
                skipSpaces()
                require(take(')')) { "Missing parenthesis" }
                return value
            }
            val start = position
            while (position < input.length && (input[position].isDigit() || input[position] == '.')) position++
            require(position > start) { "Number expected" }
            return input.substring(start, position).toDouble()
        }
        private fun take(char: Char): Boolean = if (position < input.length && input[position] == char) { position++; true } else false
        private fun skipSpaces() { while (position < input.length && input[position].isWhitespace()) position++ }
    }
}
