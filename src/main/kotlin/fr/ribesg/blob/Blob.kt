package fr.ribesg.blob

import fr.ribesg.blob.config.Config

/**
 * @author Ribesg
 */

public object Blob {

    val config: Config

    init {
        this.config = Config("blob.cfg")
    }

    public fun start() {
        this.config.load()
    }
}
