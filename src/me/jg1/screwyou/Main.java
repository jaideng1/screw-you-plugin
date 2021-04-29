package me.jg1.screwyou;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_16_R3.PacketPlayOutGameStateChange;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {
    String PLUGIN_NAME = "ScrewYou";

    public static JavaPlugin PLUGIN;

    List<UUID> guardianPlayers = new ArrayList<UUID>();
    List<UUID> closeInvPlayers = new ArrayList<UUID>();
    List<UUID> captchaPlayers = new ArrayList<UUID>();

    BukkitRunnable guardianPlayersLoop = null;
    BukkitRunnable closeInvPlayersLoop = null;

    @Override
    public void onEnable() {
        getLogger().info(PLUGIN_NAME + " has been enabled. Made by jaideng1.");

        PLUGIN = this;

        getServer().getPluginManager().registerEvents(this, this);

        startCloseInvLoop();
        startGuardianLoop();
    }

    @Override
    public void onDisable() {
        guardianPlayersLoop.cancel();
        closeInvPlayersLoop.cancel();
        getLogger().info(PLUGIN_NAME + " has been disabled.");
    }

    public void startGuardianLoop() {
        guardianPlayersLoop = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (guardianPlayers.contains(player.getUniqueId())) {
                        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.k, 0));
                    }
                }
            }
        };
        guardianPlayersLoop.runTaskTimer(this, 20, 20);
    }

    public void startCloseInvLoop() {
        closeInvPlayersLoop = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (closeInvPlayers.contains(player.getUniqueId())) {
                        player.closeInventory();
                    }
                }
            }
        };
        closeInvPlayersLoop.runTaskTimer(this, 2, 2);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (captchaPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        guardianPlayers.remove(event.getPlayer().getUniqueId());
        captchaPlayers.remove(event.getPlayer().getUniqueId());
        closeInvPlayers.remove(event.getPlayer().getUniqueId());
        //Bukkit.broadcastMessage("Removed from lists, lens: " + guardianPlayers.size() + ", " + captchaPlayers.size() + ", " + closeInvPlayers.size());

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        guardianPlayers.remove(event.getPlayer().getUniqueId());
        captchaPlayers.remove(event.getPlayer().getUniqueId());
        closeInvPlayers.remove(event.getPlayer().getUniqueId());
        //Bukkit.broadcastMessage("Removed from lists, lens: " + guardianPlayers.size() + ", " + captchaPlayers.size() + ", " + closeInvPlayers.size());
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (cmd.getName().equalsIgnoreCase("screwyou") && player.isOp()) {
                if (args.length < 2) {
                    sendHelp(player);
                    return true;
                }
                player.sendMessage(ChatColor.GREEN + "Running...");
                Player p = null;
                for (Player p_ : Bukkit.getOnlinePlayers()) {
                    if (p_.getName().equalsIgnoreCase(args[0])) {
                        p = p_;
                        break;
                    }
                }
                if (p == null) {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }

                if (args[1].equalsIgnoreCase("trialversion")) {
                    p.sendMessage(ChatColor.RED + "Your Minecraft has been changed to a free trial.");

                    final Player pf = p;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            ((CraftPlayer) pf).getHandle().playerConnection.sendPacket(new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.f, 0));
                        }
                    }.runTaskLater(this, 20 * 2);
                } else if (args[1].equalsIgnoreCase("guardian")) {
                    if (args.length < 3) {
                        player.sendMessage(ChatColor.RED + "You need to specify on or off.");
                        return true;
                    }
                    if (args[2].equalsIgnoreCase("on")) {
                        guardianPlayers.add(p.getUniqueId());
                        player.sendMessage(ChatColor.GREEN + "They have been added to the guardian visuals.");
                    } else if (args[2].equalsIgnoreCase("off")) {
                        guardianPlayers.remove(p.getUniqueId());
                        player.sendMessage(ChatColor.GREEN + "They have been removed from the guardian visuals.");
                    } else {
                        player.sendMessage(ChatColor.RED + "You need to specify on or off.");
                    }
                } else if (args[1].equalsIgnoreCase("creeper")) {
                    Vector playerDirection = p.getLocation().getDirection();
                    Location behindPlayer = p.getLocation().add(playerDirection.normalize().multiply(-1));

                    p.getWorld().playSound(behindPlayer, Sound.ENTITY_CREEPER_PRIMED, 1, 1);
                    player.sendMessage(ChatColor.GREEN + "Played sound effect.");
                } else if (args[1].equalsIgnoreCase("lag")) {
                    if (args.length < 3) {
                        player.sendMessage(ChatColor.RED + "You need to specify on or off.");
                        return true;
                    }
                    if (args[2].equalsIgnoreCase("on")) {
                        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.h, 20));
                        player.sendMessage(ChatColor.GREEN + "Started lagging out their game. For them to disable it, they have to disconnect or crash the game.");
                    } else if (args[2].equalsIgnoreCase("off")) {
                        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.h, 0));
                        player.sendMessage(ChatColor.GREEN + "Started lagging out their game. For them to disable it, they have to disconnect or crash the game.");
                    } else {
                        player.sendMessage(ChatColor.RED + "You need to specify on or off.");
                    }
                } else if (args[1].equalsIgnoreCase("loadingscreen")) {
                    player.sendMessage(ChatColor.GREEN + "Sent it. To get them out, they have to crash their game or an operator has to kick them.");
                    ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.e, 0));
                } else if (args[1].equalsIgnoreCase("nomenus")) {
                    if (args.length < 3) {
                        player.sendMessage(ChatColor.RED + "You need to specify on or off.");
                        return true;
                    }
                    if (args[2].equalsIgnoreCase("on")) {
                        player.sendMessage(ChatColor.GREEN + "Started closing their inventory.");
                        closeInvPlayers.add(p.getUniqueId());
                    } else if (args[2].equalsIgnoreCase("off")) {
                        player.sendMessage(ChatColor.GREEN + "Stopped closing their inventory.");
                        closeInvPlayers.remove(p.getUniqueId());
                    } else {
                        player.sendMessage(ChatColor.RED + "You need to specify on or off.");
                    }
                } else if (args[1].equalsIgnoreCase("fakecaptcha")) {
                    if (args.length < 3) {
                        player.sendMessage(ChatColor.RED + "You need to specify on or off.");
                        return true;
                    }
                    if (args[2].equalsIgnoreCase("on")) {
                        p.sendMessage(ChatColor.AQUA + "This is a Captcha. You cannot move until you finish it.");
                        p.sendMessage(ChatColor.RED + "If you aren't a robot hold " + ChatColor.YELLOW + "`f3`" + ChatColor.RED + " and " + ChatColor.YELLOW + "`c`" + ChatColor.RED + " - it'll send us a packet that bots cannot replicate.");
                        captchaPlayers.add(p.getUniqueId());
                        player.sendMessage(ChatColor.GREEN + "Started the fake captcha. To disable it, the player needs to disconnect, or crash their game.");
                    } else if (args[2].equalsIgnoreCase("off")) {
                        captchaPlayers.remove(p.getUniqueId());
                        player.sendMessage(ChatColor.GREEN + "Stopped the fake captcha for them.");
                    } else {
                        player.sendMessage(ChatColor.RED + "You need to specify on or off.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Unknown Argument '" + args[1] + "'");
                }
                return true;
            }
        }
        return false;
    }

    public void sendHelp(Player player) {
        player.sendMessage(ChatColor.YELLOW + "ScrewYou Help:");
        HashMap<String, String> commands = new HashMap<String, String>();
        commands.put("trialversion", "Displays the Free Trial Screen.");
        commands.put("guardian <on/off>", "Spams the Guardian graphics on a player.");
        commands.put("creeper", "Plays the Creeper sound from behind the player.");
        commands.put("lag <on/off>", "Lags the player's game.");
        commands.put("loadingscreen", "Puts the player into a loading screen. The only way for them to get out of it is to close their game or to be kicked.");
        commands.put("nomenus <on/off>", "Constantly closes whatever menus they have open.");
        commands.put("fakecaptcha <on/off>", "Asks them to do a captcha which in lead will ask them to crash their game.");
        for (String cmd : commands.keySet()) {
            player.sendMessage(ChatColor.BLUE + "/screwyou <player> " + cmd + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + commands.get(cmd));
        }
    }
}
