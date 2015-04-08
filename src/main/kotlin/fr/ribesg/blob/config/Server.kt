package fr.ribesg.blob.config

/**
 * @author Ribesg
 */

public data class Server(
        val name: String,
        val nick: String,
        val host: String,
        val port: Int,
        val channels: List<String>,
        val pass: String? = null
)
