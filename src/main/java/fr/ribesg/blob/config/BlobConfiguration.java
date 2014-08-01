package fr.ribesg.blob.config;
import fr.ribesg.alix.api.bot.config.AlixConfiguration;
import fr.ribesg.alix.api.bot.util.configuration.YamlDocument;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/** @author Ribesg */
public class BlobConfiguration extends AlixConfiguration {

   private String      wolframAlphaAppId;
   private String      esperNetNickServPass;
   private boolean     espernNetNickServAutoIdentify;
   private Set<String> admins;

   public BlobConfiguration() {
      super("blob.yml");
      this.wolframAlphaAppId = "CHANGEME";
      this.esperNetNickServPass = "CHANGEME";
      this.espernNetNickServAutoIdentify = false;
      this.admins = new HashSet<>();
   }

   @Override
   protected void loadMainAdditional(final YamlDocument mainDocument) {
      this.wolframAlphaAppId = mainDocument.getString("wolframAlphaAppId", this.wolframAlphaAppId);
      this.esperNetNickServPass = mainDocument.getString("esperNetNickServPass", this.esperNetNickServPass);
      this.espernNetNickServAutoIdentify = mainDocument.getBoolean("espernNetNickServAutoIdentify", this.espernNetNickServAutoIdentify);
      this.admins.addAll(mainDocument.getStringList("admins", Arrays.asList("Ribesg")));
   }

   @Override
   protected void saveMainAdditional(final YamlDocument mainDocument) {
      mainDocument.set("wolframAlphaAppId", this.wolframAlphaAppId);
      mainDocument.set("esperNetNickServPass", this.esperNetNickServPass);
      mainDocument.set("espernNetNickServAutoIdentify", this.espernNetNickServAutoIdentify);
      mainDocument.set("admins", new LinkedList<>(this.admins));
   }

   public String getWolframAlphaAppId() {
      return this.wolframAlphaAppId;
   }

   public String getEsperNetNickServPass() {
      return this.esperNetNickServPass;
   }
   
   public boolean hasEsperNetNickServAutoIdentify() {
      return this.espernNetNickServAutoIdentify;
   }

   public Set<String> getAdmins() {
      return this.admins;
   }
}
