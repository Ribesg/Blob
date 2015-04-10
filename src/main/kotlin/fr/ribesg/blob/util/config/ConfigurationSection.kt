package fr.ribesg.blob.util.config

import java.util.LinkedHashMap

/**
 * @author Ribesg
 */

public open class ConfigurationSection(val map: MutableMap<String, Any> = LinkedHashMap()) {

    // Generic functions
    public fun hasOfType(key: String, clazz: Class<*>): Boolean {
        val o = this.map[key]
        return o != null && clazz.isInstance(o)
    }

    [suppress("BASE_WITH_NULLABLE_UPPER_BOUND")] // This warning may be a bug
    public fun <T> getAs(key: String, clazz: Class<T>): T? {
        return if (this.hasOfType(key, clazz)) clazz.cast(this.map[key]) else null
    }

    public fun set(key: String, o: Any?) {
        when (o) {
            null                    -> this.map.remove(key)
            is ConfigurationSection -> this.map[key] = o.map
            else                    -> this.map[key] = o
        }
    }

    // String shortcuts
    public fun isString(key: String): Boolean
            = this.hasOfType(key, javaClass<String>())

    public fun getString(key: String): String?
            = this.getAs(key, javaClass<String>())

    public fun getString(key: String, default: String): String
            = this.getString(key) ?: default

    // Int shortcuts
    public fun isInt(key: String): Boolean
            = this.hasOfType(key, javaClass<Int>())

    public fun getInt(key: String): Int?
            = this.getAs(key, javaClass<Int>())

    public fun getInt(key: String, default: Int): Int
            = this.getInt(key) ?: default

    // Boolean shortcuts
    public fun isBoolean(key: String): Boolean
            = this.hasOfType(key, javaClass<Boolean>())

    public fun getBoolean(key: String): Boolean?
            = this.getAs(key, javaClass<Boolean>())

    public fun getBoolean(key: String, default: Boolean): Boolean
            = this.getBoolean(key) ?: default

    // List shortcuts
    public fun isList(key: String): Boolean
            = this.hasOfType(key, javaClass<List<*>>())

    public fun getList(key: String): List<*>?
            = this.getAs(key, javaClass<List<*>>())

    public fun getList(key: String, default: List<*>): List<*>
            = this.getList(key) ?: default

    // List<T> shortcuts
    public fun <T> isListOf(key: String, clazz: Class<T>): Boolean
            = this.hasOfType(key, javaClass<List<T>>())

    public fun <T> getListOf(key: String, clazz: Class<T>): List<T>?
            = this.getAs(key, javaClass<List<T>>())

    public fun <T> getListOf(key: String, clazz: Class<T>, default: List<T>): List<T>
            = this.getListOf(key, clazz) ?: default

    // ConfigurationSection shortcuts
    public fun isSection(key: String): Boolean
            = this.hasOfType(key, javaClass<Map<String, Any>>())

    public fun getSection(key: String): ConfigurationSection?
            = this.getAs(key, javaClass<MutableMap<String, Any>>())?.let { ConfigurationSection(it) }

    public fun getOrCreateSection(key: String): ConfigurationSection
            = this.getSection(key) ?: this.map.put(key, ConfigurationSection()) as ConfigurationSection

    // Others
    public fun getKeys(): Set<String> = this.map.keySet()

    override fun equals(other: Any?): Boolean = when {
        this == other                  -> true
        other !is ConfigurationSection -> false
        this.map.equals(other.map)     -> true
        else                           -> false
    }

    override fun hashCode(): Int = this.map.hashCode()
}
