package cc.mousse.steward;

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * @author MochaMousse
 */
public class StewardExchangeSpigot extends JavaPlugin implements Listener, CommandExecutor {
  public static final String CHANNEL_NAME = "steward:exchange";
  public static final String AI_COMMAND = "ai";
  public static final Gson GSON = new Gson();
  private static String SERVER_NAME;
  private static String AI_NAME;
  private static Boolean AI_ENABLE;

  @EventHandler
  public void onPlayerChat(AsyncPlayerChatEvent event) {
    getServer()
        .sendPluginMessage(
            this,
            CHANNEL_NAME,
            GSON.toJson(new Letter(0, SERVER_NAME, event.getPlayer().getName(), event.getMessage()))
                .getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public void onEnable() {
    loadConfig();
    Objects.requireNonNull(getCommand(AI_COMMAND)).setExecutor(this);
    getServer().getPluginManager().registerEvents(this, this);
    getServer().getMessenger().registerOutgoingPluginChannel(this, CHANNEL_NAME);
    getServer()
        .getMessenger()
        .registerIncomingPluginChannel(
            this,
            CHANNEL_NAME,
            (channel, player, message) -> {
              if (CHANNEL_NAME.equals(channel)) {
                Letter letter = GSON.fromJson(new String(message), Letter.class);
                if (letter != null) {
                  switch (letter.ai) {
                    case 0 -> {
                      if (!Objects.equals(letter.getServer(), SERVER_NAME)) {
                        Bukkit.broadcastMessage(
                            ChatColor.GRAY
                                + String.format(
                                    "<%s> %s", letter.getPlayer(), letter.getContent()));
                      }
                    }
                    case 1 -> {
                      if (!Objects.equals(letter.getServer(), SERVER_NAME)) {
                        Bukkit.broadcastMessage(
                            ChatColor.GRAY
                                + String.format(
                                    "<%s → %s> %s",
                                    letter.getPlayer(), AI_NAME, letter.getContent()));
                      }
                    }
                    default ->
                        Bukkit.broadcastMessage(
                            ChatColor.DARK_AQUA
                                + String.format(
                                    "<%s → %s> %s",
                                    AI_NAME, letter.getPlayer(), letter.getContent()));
                  }
                }
              }
            });
  }

  @Override
  public void onDisable() {
    getServer().getMessenger().unregisterOutgoingPluginChannel(this, CHANNEL_NAME);
    getServer().getMessenger().unregisterIncomingPluginChannel(this, CHANNEL_NAME);
  }

  @Override
  public boolean onCommand(
      @NonNull CommandSender sender,
      @NonNull org.bukkit.command.Command command,
      @NonNull String label,
      @NotNull String[] args) {
    if (AI_COMMAND.equalsIgnoreCase(command.getName())) {
      if (sender instanceof Player) {
        String content = String.join(" ", args);
        if (AI_ENABLE) {
          getServer()
              .sendPluginMessage(
                  this,
                  CHANNEL_NAME,
                  GSON.toJson(new Letter(1, SERVER_NAME, sender.getName(), content))
                      .getBytes(StandardCharsets.UTF_8));
          Bukkit.broadcastMessage(
              String.format("<%s → %s> %s", sender.getName(), AI_NAME, content));
        } else {
          Bukkit.broadcastMessage(String.format("<%s> %s", sender.getName(), content));
        }
      } else {
        String reloadName = "reload";
        if (args.length == 1 && reloadName.equalsIgnoreCase(args[0])) {
          loadConfig();
          sender.sendMessage("配置已重载");
        }
      }
    }
    return true;
  }

  private void loadConfig() {
    saveDefaultConfig();
    reloadConfig();
    FileConfiguration config = getConfig();
    SERVER_NAME = config.getString("server.name");
    AI_ENABLE = config.getBoolean("ai.enable");
    AI_NAME = config.getString("ai.name");
  }

  @Data
  @AllArgsConstructor
  public static class Letter {
    private Integer ai;
    private String server;
    private String player;
    private String content;
  }
}
