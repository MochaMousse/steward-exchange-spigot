package cc.mousse.steward;

import com.google.gson.Gson;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author MochaMousse
 */
public class StewardExchangeSpigot extends JavaPlugin implements Listener {
  public static final Gson GSON = new Gson();
  public static final String CHANNEL_NAME = "steward:exchange";
  private static final Set<String> PLAYERS = new HashSet<>();

  @Override
  public void onEnable() {
    saveDefaultConfig();
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
                if (letter != null && !PLAYERS.contains(letter.getPlayer())) {
                  Bukkit.broadcastMessage(
                      ChatColor.GRAY
                          + String.format("<%s> %s", letter.getPlayer(), letter.getContent()));
                }
              }
            });
  }

  @Override
  public void onDisable() {
    getServer().getMessenger().unregisterOutgoingPluginChannel(this, CHANNEL_NAME);
    getServer().getMessenger().unregisterIncomingPluginChannel(this, CHANNEL_NAME);
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    PLAYERS.add(event.getPlayer().getName());
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    PLAYERS.remove(event.getPlayer().getName());
  }
}
