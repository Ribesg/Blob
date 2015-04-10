package fr.ribesg.blob

import fr.ribesg.blob.command.CommandManager
import fr.ribesg.blob.config.Config
import fr.ribesg.blob.util.Log
import org.kitteh.irc.client.library.Client
import org.kitteh.irc.client.library.ClientBuilder
import java.util.HashMap

/**
 * @author Ribesg
 */

public object Blob {

    private val config: Config

    private val clients: MutableMap<String, Client>
    private val commandManager: CommandManager

    init {
        Log.info("Starting Blob")
        this.config = Config("blob.cfg")
        this.clients = HashMap()
        this.commandManager = CommandManager()
    }

    public fun start() {
        Log.info("Loading Configuration")
        if (!this.config.load()) {
            Log.info("New configuration file created, please configure the bot and restart it")
            return
        }

        this.config.servers.forEach {
            val server = it
            val builder = ClientBuilder()

            builder.nick(server.nick)
            builder.realName(this.config.mainNick)
            builder.user(this.config.mainNick)
            builder.name(server.name)

            builder.server(server.host).server(server.port)
            if (server.pass != null) builder.serverPassword(server.pass)

            builder.listenException {
                Log.error("ERROR    @ " + server.name, it)
            }

            builder.listenInput {
                Log.debug("RECEIVED @ " + server.name + ": " + it)
            }

            builder.listenOutput {
                Log.debug("SENT     @ " + server.name + ": " + it)
            }

            val client = builder.build()

            client.getEventManager().registerEventListener(this.commandManager)

            server.channels.forEach { client.addChannel(it) }

            this.clients[it.getKey()] = client
        }
    }

    public fun stop(reason: String) {
        Log.info("Stopping bot: " + reason)
        this.clients.values().forEach { it.shutdown(reason) }
    }
}
