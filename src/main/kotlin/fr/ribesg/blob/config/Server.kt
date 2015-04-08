package fr.ribesg.blob.config

/**
 * @author Ribesg
 */

public data class Server(
        val name: String,
        val host: String,
        val port: Int,
        val channels: List<String>,
        val nick: String,
        val user: String,
        val pass: String? = null
) {
    public fun getKey(): String = this.host + ':' + this.port
}
