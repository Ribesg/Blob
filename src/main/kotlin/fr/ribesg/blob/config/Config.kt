package fr.ribesg.blob.config

import fr.ribesg.blob.util.Log
import fr.ribesg.blob.util.config.YamlDocument
import fr.ribesg.blob.util.config.YamlFile
import java.nio.file.Files
import java.nio.file.Paths
import java.util.LinkedList
import java.util.regex.Pattern

/**
 * @author Ribesg
 */

public class Config(val fileName: String) {

    public companion object {
        public fun getDefaultBotConfig(fileName: String): Config {
            val result = Config(fileName)
            result.servers.add(Server("EsperNet", result.mainNick, "irc.esper.net", 6667, listOf("#blob")))
            return result
        }
    }

    public val servers: MutableList<Server> = LinkedList()
    public var mainNick: String = "Blob"
    private val file: YamlFile = YamlFile(this.fileName)

    public fun exists(): Boolean = Files.exists(Paths.get(this.fileName))

    public fun load(): Boolean {
        return if (this.exists()) {
            this.file.load()

            val firstDocument = this.file.docs.first()
            this.mainNick = firstDocument.getString("mainNick", "Blob")

            for (i in 1..this.file.docs.lastIndex) try {
                val doc = this.file.docs[i]
                val name = doc.getString("name")!!
                val host = doc.getString("host")!!
                val port = doc.getInt("port", 6667)
                val pass = doc.getString("password")
                val channels = doc.getListOf("channels", javaClass<String>(), listOf("#blob"))
                val nick = doc.getString("nick", this.mainNick)

                this.servers.add(Server(name, nick, host, port, channels, pass))

                if (pass != null) {
                    Log.addFilter(Pattern.quote(pass), "**********")
                }
            } catch (e: NullPointerException) {
                Log.fatal("Invalid configuration", e)
                System.exit(42)
            }
            true
        } else {
            this.newConfig()
            false;
        }
    }

    public fun save() {
        this.file.docs.clear()

        val firstDocument = YamlDocument()
        firstDocument.set("mainNick", this.mainNick)
        this.file.docs.add(firstDocument)

        for (i in 1..this.servers.lastIndex) {
            val doc = YamlDocument()
            val server = this.servers[i]
            doc.set("name", server.name)
            doc.set("host", server.host)
            doc.set("port", server.port)
            server.pass?.let { doc.set("password", server.pass) }
            doc.set("channels", server.channels)
            doc.set("nick", server.nick)
            this.file.docs.add(doc)
        }

        this.file.save()
    }

    private fun newConfig() {
        Config.getDefaultBotConfig(this.fileName).save()
        this.load()
    }
}