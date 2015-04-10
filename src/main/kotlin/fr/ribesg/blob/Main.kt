package fr.ribesg.blob

/**
 * @author Ribesg
 */

fun main(args: Array<String>) {
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            Blob.stop("Bot killed!")
        }
    })

    Blob.start()
}
