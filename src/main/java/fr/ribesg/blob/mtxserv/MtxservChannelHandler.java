package fr.ribesg.blob.mtxserv;

import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Log;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.util.configuration.YamlDocument;
import fr.ribesg.alix.api.bot.util.configuration.YamlFile;
import fr.ribesg.blob.BlobClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MtxservChannelHandler {

   private static final String CONFIG_FILE = "mtx.yml";

   private final BlobClient client;

   boolean enabled;
   final List<String> admins;
   final List<String> pingables;
   final List<String> welcomeMessage;

   final List<String> seenBeforeList;

   public MtxservChannelHandler(final BlobClient client) {
      this.client = client;
      this.enabled = true;
      this.admins = new CopyOnWriteArrayList<>();
      this.pingables = new CopyOnWriteArrayList<>();
      this.welcomeMessage = new CopyOnWriteArrayList<>();
      this.seenBeforeList = new CopyOnWriteArrayList<>();
      this.load();
   }

   public void load() {
      final Path path = Paths.get(CONFIG_FILE);
      if (!Files.exists(path)) {
         this.admins.add("mTxServ^Snk");
         this.pingables.add("mTxServ^Snk");
         this.welcomeMessage.add("Bienvenue ### !");
         this.seenBeforeList.add("mTxServ^Snk");
         this.save();
      } else {
         final YamlFile yamlFile = new YamlFile();
         try {
            yamlFile.load(CONFIG_FILE);
         } catch (final IOException e) {
            Log.error(e.getMessage(), e);
            return;
         }

         final YamlDocument yaml = yamlFile.getDocuments().get(0);

         this.enabled = yaml.getBoolean("enabled", true);

         if (yaml.isStringList("admins")) {
            this.admins.addAll(yaml.getStringList("admins"));
         } else {
            Log.warn("Missing or invalid value for entry 'admins' in mtx.yml");
         }

         if (yaml.isStringList("pingables")) {
            this.pingables.addAll(yaml.getStringList("pingables"));
         } else {
            Log.warn("Missing or invalid value for entry 'pingables' in mtx.yml");
         }

         if (yaml.isStringList("welcomeMessage")) {
            this.welcomeMessage.addAll(yaml.getStringList("welcomeMessage"));
         } else {
            Log.warn("Missing or invalid value for entry 'welcomeMessage' in mtx.yml");
         }

         if (yaml.isStringList("seenBeforeList")) {
            this.seenBeforeList.addAll(yaml.getStringList("seenBeforeList"));
         } else {
            Log.warn("Missing or invalid value for entry 'seenBeforeList' in mtx.yml");
         }
      }
   }

   public void save() {
      final Path path = Paths.get(CONFIG_FILE);
      if (!Files.exists(path)) {
         try {
            Files.createFile(path);
         } catch (IOException e) {
            Log.error(e.getMessage(), e);
            return;
         }
      }

      final YamlFile yamlFile = new YamlFile();
      final YamlDocument yaml = new YamlDocument();

      yaml.set("enabled", this.enabled);
      yaml.set("admins", this.admins);
      yaml.set("pingables", this.pingables);
      yaml.set("welcomeMessage", this.welcomeMessage);
      yaml.set("seenBeforeList", this.seenBeforeList);

      yamlFile.getDocuments().add(yaml);
      try {
         yamlFile.save(CONFIG_FILE);
      } catch (final IOException e) {
         Log.error(e.getMessage(), e);
      }
   }

   boolean hasAdmin(final Source user) {
      // TODO Check NickServ
      return client.getCommandManager().getBotAdmins().contains(user.getName()) || this.admins.contains(user.getName());
   }

   public void onClientJoinChannel(final Channel channel) {
      if (this.enabled && "#mtxserv".equals(channel.getName()) && channel.getServer().getUrl().contains("quakenet")) {
         this.seenBeforeList.addAll(channel.getUserNicknames());
         this.save();
      }
   }


   public void onUserJoinChannel(final Source user, final Channel channel) {
      if (this.enabled && "#mtxserv".equals(channel.getName()) && channel.getServer().getUrl().contains("quakenet")) {
         final String userName = user.getName();
         if (!this.seenBeforeList.contains(userName) && !this.admins.contains(userName)) {
            this.seenBeforeList.add(userName);
            this.welcomeMessage.forEach((message) -> channel.sendMessage(message.replace("###", userName)));

            if (!this.pingables.isEmpty()) {
               final StringBuilder builder = new StringBuilder("Ping " + this.pingables.get(0));
               for (int i = 1; i < this.pingables.size(); i++) {
                  builder.append(", ").append(this.pingables.get(i));
               }
               channel.sendMessage(builder.toString());
            }
            this.save();
         }
      }
   }
}
