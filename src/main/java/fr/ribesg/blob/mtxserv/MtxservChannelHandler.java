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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

public class MtxservChannelHandler {

   private static final String CONFIG_FILE = "mtx.yml";

   private final BlobClient client;

   boolean enabled;
   final Set<String> admins;
   final Set<String> pingables;
   final List<String> welcomeMessage;

   final Set<String> seenBeforeList;

   public MtxservChannelHandler(final BlobClient client) {
      this.client = client;
      this.enabled = true;
      this.admins = new ConcurrentSkipListSet<>();
      this.pingables = new ConcurrentSkipListSet<>();
      this.welcomeMessage = new CopyOnWriteArrayList<>();
      this.seenBeforeList = new ConcurrentSkipListSet<>();
      this.load();
   }

   public void load() {
      final Path path = Paths.get(CONFIG_FILE);
      if (!Files.exists(path)) {
         this.admins.add("mTxServ^Snk");
         this.pingables.add("mTxServ^Snk");
         this.welcomeMessage.add("Bienvenue %% !");
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
      yaml.set("admins", new ArrayList<>(this.admins));
      yaml.set("pingables", new ArrayList<>(this.pingables));
      yaml.set("welcomeMessage", this.welcomeMessage);
      yaml.set("seenBeforeList", new ArrayList<>(this.seenBeforeList));

      yamlFile.getDocuments().add(yaml);
      try {
         yamlFile.save(CONFIG_FILE);
      } catch (final IOException e) {
         Log.error(e.getMessage(), e);
      }
   }

   boolean isAdmin(final Source user) {
      // TODO Check NickServ
      return client.getCommandManager().getBotAdmins().contains(user.getName()) || this.admins.contains(user.getName());
   }

   public void onClientJoinChannel(final Channel channel) {
      if (this.enabled && "#mtxserv".equals(channel.getName()) && "QuakeNet".equals(channel.getServer().getName())) {
         this.seenBeforeList.addAll(channel.getUserNicknames());
         this.save();
      }
   }


   public void onUserJoinChannel(final Source user, final Channel channel) {
      if (this.enabled && "#mtxserv".equals(channel.getName()) && "QuakeNet".equals(channel.getServer().getName())) {
         final String userName = user.getName();
         if (!this.seenBeforeList.contains(userName) && !this.admins.contains(userName)) {
            this.seenBeforeList.add(userName);
            this.welcomeMessage.forEach((message) -> channel.sendMessage(message.replace("%%", userName)));

            if (!this.pingables.isEmpty()) {
               final Iterator<String> it = this.pingables.iterator();
               final StringBuilder builder = new StringBuilder("Ping " + it.next());
               while (it.hasNext()) {
                  builder.append(", ").append(it.next());
               }
               channel.sendMessage(builder.toString());
            }
            this.save();
         }
      }
   }
}
