package fr.ribesg.blob.util.config

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.LinkedList

/**
 * @author Ribesg
 */

public class YamlFile(filePath: String) {

    private companion object {

        val CHARSET = StandardCharsets.UTF_8
        val YAML: Yaml

        init {
            val options = DumperOptions()
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
            options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN)
            this.YAML = Yaml(options)
        }
    }

    public val docs: MutableList<YamlDocument> = LinkedList()
    private val filePath = Paths.get(filePath)

    public fun load() {
        if (!Files.exists(this.filePath)) {
            Files.createFile(this.filePath)
        } else {
            this.loadFromString(Files.readAllLines(this.filePath, YamlFile.CHARSET).join("\n"))
        }
    }

    public fun loadFromString(string: String) {
        YamlFile.YAML.loadAll(string).forEach {
            [suppress("UNCHECKED_CAST")]
            this.docs.add(YamlDocument(it as MutableMap<String, Any>))
        }
    }

    public fun save() {
        Files.write(
                this.filePath,
                this.saveToString().split("\n").asIterable(),
                YamlFile.CHARSET,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE
        )
    }

    public fun saveToString(): String {
        val raw = LinkedList<Map<String, Any>>()
        this.docs.forEach {
            raw.add(it.asMap())
        }
        return YamlFile.YAML.dumpAll(raw.iterator())
    }

    override fun equals(other: Any?): Boolean {
        return when {
            this == other                -> true
            other !is YamlFile           -> false
            this.docs.equals(other.docs) -> true
            else                         -> false
        }
    }

    override fun hashCode(): Int = this.docs.hashCode()
}
