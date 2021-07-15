package org.evomaster.core.search.service

import com.google.inject.Inject
import org.evomaster.core.EMConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import javax.annotation.PostConstruct


class Randomness {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(Randomness::class.java)
    }

    @Inject
    private lateinit var configuration: EMConfig

    private val random = Random()

    @PostConstruct
    private fun initialize() {
        updateSeed(configuration.seed)
    }

    private val wordChars = "_0123456789abcdefghilmnopqrstuvzjkwxyABCDEFGHILMNOPQRSTUVZJKWXY"
            .map { it.toInt() }.sorted()

    /**
     * A negative value means the current CPU time clock is used instead
     */
    fun updateSeed(seed: Long) {
        if (seed < 0) {
            random.setSeed(System.currentTimeMillis())
        } else {
            random.setSeed(seed)
        }
    }

    fun nextBoolean(): Boolean {
        val k = random.nextBoolean()
        log.trace("nextBoolean(): {}", k)
        return k
    }

    /**
     * Return true with probability P
     */
    fun nextBoolean(p: Double): Boolean {
        val k = random.nextDouble() < p
        log.trace("nextBoolean(): {}", k)
        return k
    }

    fun nextInt(): Int {
        val k = random.nextInt()
        log.trace("nextInt(): {}", k)
        return k
    }

    fun nextDouble(): Double {
        val k = random.nextDouble()
        log.trace("nextDouble(): {}", k)
        return k
    }

    fun nextGaussian(): Double {
        val k = random.nextGaussian()
        log.trace("nextGaussian(): {}", k)
        return k
    }

    fun nextFloat(): Float {
        val k = random.nextFloat()
        log.trace("nextFloat(): {}", k)
        return k
    }

    fun nextInt(bound: Int): Int {
        val k = random.nextInt(bound)
        log.trace("nextInt(bound): {} , {}", k, bound)
        return k
    }


    fun nextInt(min: Int, max: Int, exclude: Int): Int {

        if (min == max && max == exclude) {
            throw IllegalArgumentException("Nothing to select, as min/max/exclude are all equal")
        }

        var k = nextInt(min, max)
        while (k == exclude) {
            k = nextInt(min, max)
        }
        log.trace("nextInt(min,max,exclude): {}", k)
        return k
    }

    /**
     * A random int between "min" and "max", both inclusive
     */
    fun nextInt(min: Int, max: Int): Int {
        if (min == max) {
            return min
        }
        if (min > max) {
            throw IllegalArgumentException("Min $min is bigger than max $max")
        }

        val k = (min.toLong() + random.nextDouble() * (max.toLong() - min + 1)).toInt()
        log.trace("nextInt(min,max): {}", k)
        return k
    }


    fun nextLong(): Long {
        val k = random.nextLong()
        log.trace("nextLong(): {}", k)
        return k
    }

    fun nextWordString(min: Int = 0, max: Int = 10): String {

        val n = nextInt(min, max)

        val chars = CharArray(n)
        (0 until n).forEach {
            chars[it] = nextWordChar()
        }

        val k = String(chars)
        log.trace("nextWordString(): {}", k)
        return k
    }


    fun nextLetter(): Char {

        val characters = "abcdefghilmnopqrstuvzjkwxyABCDEFGHILMNOPQRSTUVZJKWXY"

        val k = characters[random.nextInt(characters.length)]
        log.trace("nextLetter(): {}", k)
        return k
    }

    fun nextWordChar(): Char {

        val characters = "_0123456789abcdefghilmnopqrstuvzjkwxyABCDEFGHILMNOPQRSTUVZJKWXY"

        val k = characters[random.nextInt(characters.length)]
        log.trace("nextWordChar(): {}", k)
        return k
    }

    fun wordCharPool() = wordChars

    fun validNextWordChars(min: Int, max: Int): List<Int> {
        return wordChars.filter { it in min..max }
    }

    fun nextChar(min: Int = Char.MIN_VALUE.toInt(), max: Int = Char.MAX_VALUE.toInt()): Char {
        if (min < Char.MIN_VALUE.toInt())
            throw IllegalArgumentException("$min is less than MIN_VALUE of Char")
        if (max > Char.MAX_VALUE.toInt())
            throw IllegalArgumentException("$max is more than MAX_VALUE of Char")
        if (min > max)
            throw IllegalArgumentException("$min should be less than $max")
        if (min == max)
            return min.toChar()
        return choose((min..max).toList()).toChar()
    }

    fun nextChar(start: Char, endInclusive: Char): Char {

        if (start > endInclusive) {
            throw IllegalArgumentException("Start '$start' is after end '$endInclusive'")
        }

        if (start == endInclusive) {
            return start
        }

        return nextInt(start.toInt(), endInclusive.toInt()).toChar()
    }

    /**
     * Choose a value from the [map] based on the associated probabilities.
     * The highest the associated probability, the *more* chances to be selected.
     * If an element [K] is not present in the map, then
     * its probability is 0.
     *
     * Note: as [K] is used as a key, make sure that [equals] and [hashCode]
     * are well defined for it (eg, no problem if it is a [Int] or a [String])
     */
    fun <K> chooseByProbability(map: Map<K, Float>): K {

        val randFl = random.nextFloat() * map.values.sum()
        var temp = 0.toFloat()
        var found = map.keys.first()

        for ((k, v) in map) {
            if (randFl <= (v + temp)) {
                found = k
                break
            }
            temp += v
        }

        log.trace("Chosen: {}", found)

        return found
    }

    /**
     * Choose a value from the [list] based on its weight in the [weights] map.
     * The highest the weight, the *less* chances to be selected.
     * If an element [K] is not present in the map [weights], then
     * its weight is 0.
     * Note: as [K] is used as a key, make sure that [equals] and [hashCode]
     * are well defined for it (eg, no problem if it is a [Int] or a [String])
     */
    fun <K, V> chooseProportionally(list: List<K>, weights: Map<K, V>): K {

        throw IllegalStateException("Not implemented yet")
    }

    /**
     * Choose a random element from the [list], up to the element in the index position (exclusive).
     * Ie, consider only the first [index] elements
     */
    fun <T> chooseUpTo(list: List<T>, index: Int): T {
        if (index <= 0 || index > list.size) {
            throw IllegalArgumentException("Invalid index $index in list of size ${list.size}")
        }

        val index = random.nextInt(index)
        return list[index]
    }

    fun <T> choose(list: List<T>): T {
        if (list.isEmpty()) {
            throw IllegalArgumentException("Empty list to choose from")
        }
        return chooseUpTo(list, list.size)
    }

    /**
     * Randomly choose (without replacement) up to [n] values from the [list]
     */
    fun <T> choose(list: List<T>, n: Int): List<T> {
        if (list.size <= n) {
            return list
        }

        val selection: MutableList<T> = mutableListOf()
        selection.addAll(list)
        selection.shuffle(random)

        val k =  selection.subList(0, n)

        if(log.isTraceEnabled) log.trace("Chosen: {}", k.joinToString(" "))

        return k
    }

    /**
     * Randomly choose (without replacement) up to [n] values from the [set]
     */
    fun <T> choose(set: Set<T>, n: Int): Set<T> {
        if (set.size <= n) {
            return set
        }

        val selection: MutableList<T> = mutableListOf()
        selection.addAll(set)
        selection.shuffle(random)

        val k = selection.subList(0, n).toSet()

        if(log.isTraceEnabled) log.trace("Chosen: {}", k.joinToString(" "))

        return k
    }


    fun <K, V> choose(map: Map<K, V>): V = choose(map.values)


    /**
     * Randomly choose one element
     */
    fun <V> choose(collection: Collection<V>): V {
        if (collection.isEmpty()) {
            throw IllegalArgumentException("Empty map to choose from")
        }
        val index = random.nextInt(collection.size)
        var i = 0

        val iter = collection.iterator()
        while (iter.hasNext() && i < index) {
            iter.next()
            i++
        }

        val k = iter.next()
        log.trace("Chosen: {}", k)

        return k
    }

    /**
     * Randomly shuffle an iterable
     */
    fun <T> shuffle(it: Iterable<T>): List<T> {
        return it.shuffled(random)
    }
}