package fr.ribesg.blob.util.config

import java.util.LinkedHashMap

/**
 * @author Ribesg
 */

public class YamlDocument(map: MutableMap<String, Any> = LinkedHashMap()) : ConfigurationSection(map) {

    public fun asMap(): Map<String, Any> = this.map
}
