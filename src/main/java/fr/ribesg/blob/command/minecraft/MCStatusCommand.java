package fr.ribesg.blob.command.minecraft;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.ribesg.alix.api.Channel;
import fr.ribesg.alix.api.Receiver;
import fr.ribesg.alix.api.Server;
import fr.ribesg.alix.api.Source;
import fr.ribesg.alix.api.bot.command.Command;
import fr.ribesg.alix.api.bot.command.CommandManager;
import fr.ribesg.alix.api.bot.util.WebUtil;
import fr.ribesg.alix.api.enums.Codes;
import org.apache.log4j.Logger;

public class MCStatusCommand extends Command {

	private static final Logger LOGGER = Logger.getLogger(MCStatusCommand.class.getName());

	private static final String XPAW_MCSTATUS_URL = "http://xpaw.ru/mcstatus/status.json";

	public MCStatusCommand(final CommandManager manager) {
		super(manager, "mcstatus", new String[] {" - Get the state of the Minecraft services"}, "mcs");
	}

	@Override
	public void exec(final Server server, final Channel channel, final Source user, final String primaryArgument, final String[] args) {
		final Receiver receiver = channel == null ? user : channel;

		try {
			final String jsonString = WebUtil.getString(XPAW_MCSTATUS_URL);

			final JsonObject json = (JsonObject) new JsonParser().parse(jsonString);
			final JsonObject report = json.getAsJsonObject("report");

			final boolean login = "up".equals(report.getAsJsonObject("login").getAsJsonPrimitive("status").getAsString());
			final boolean session = "up".equals(report.getAsJsonObject("session").getAsJsonPrimitive("status").getAsString());
			final boolean website = "up".equals(report.getAsJsonObject("website").getAsJsonPrimitive("status").getAsString());
			final boolean skins = "up".equals(report.getAsJsonObject("skins").getAsJsonPrimitive("status").getAsString());
			final boolean realms = "up".equals(report.getAsJsonObject("realms").getAsJsonPrimitive("status").getAsString());

			final StringBuilder message = new StringBuilder();
			message.append(login ? Codes.LIGHT_GREEN : Codes.RED).append(Codes.BOLD).append("Login");
			message.append(Codes.RESET).append(Codes.LIGHT_GRAY).append(" - ");
			message.append(session ? Codes.LIGHT_GREEN : Codes.RED).append(Codes.BOLD).append("Session");
			message.append(Codes.RESET).append(Codes.LIGHT_GRAY).append(" - ");
			message.append(website ? Codes.LIGHT_GREEN : Codes.RED).append(Codes.BOLD).append("Website");
			message.append(Codes.RESET).append(Codes.LIGHT_GRAY).append(" - ");
			message.append(skins ? Codes.LIGHT_GREEN : Codes.RED).append(Codes.BOLD).append("Skins");
			message.append(Codes.RESET).append(Codes.LIGHT_GRAY).append(" - ");
			message.append(realms ? Codes.LIGHT_GREEN : Codes.RED).append(Codes.BOLD).append("Realms");

			receiver.sendMessage(message.toString());
		} catch (final Exception e) {
			receiver.sendMessage(Codes.RED + "Failed to ping/parse status");
			LOGGER.error("Failed to ping/parse " + XPAW_MCSTATUS_URL, e);
		}
	}
}
