
package me.netherportal;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class NetherPortal extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("NetherPortal is enabled!");
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        String message = PlainTextComponentSerializer.plainText()
                .serialize(event.message()).trim();

        if (!message.equalsIgnoreCase("Nether")) return;

        event.setCancelled(true);

        Player player = event.getPlayer();

        Bukkit.getScheduler().runTask(this, () -> createPortal(player));
    }

    private void createPortal(Player player) {
        if (!player.isOnline()) return;

        Location loc = player.getLocation();
        World world = loc.getWorld();

        double yaw = Math.toRadians(loc.getYaw());
        int dx = (int) Math.round(-Math.sin(yaw));
        int dz = (int) Math.round(Math.cos(yaw));

        int centerX = loc.getBlockX() + dx * 2;
        int centerZ = loc.getBlockZ() + dz * 2;
        int baseY = loc.getBlockY();

        Axis axis = Math.abs(dz) >= Math.abs(dx)
                ? Axis.X : Axis.Z;

        int widthX = axis == Axis.X ? 1 : 0;
        int widthZ = axis == Axis.Z ? 1 : 0;

        // Check all portal blocks before building.
        for (int w = 0; w < 4; w++) {
            for (int h = 0; h < 5; h++) {
                int x = centerX + (w - 1) * widthX;
                int y = baseY + h;
                int z = centerZ + (w - 1) * widthZ;

                if (y < world.getMinHeight()
                        || y >= world.getMaxHeight()
                        || !world.getBlockAt(x, y, z).isReplaceable()) {
                    player.sendMessage(
                            ChatColor.RED + "Not enough clear space for a portal!"
                    );
                    return;
                }
            }
        }

        // Build the obsidian frame.
        for (int w = 0; w < 4; w++) {
            for (int h = 0; h < 5; h++) {
                int x = centerX + (w - 1) * widthX;
                int y = baseY + h;
                int z = centerZ + (w - 1) * widthZ;

                Block block = world.getBlockAt(x, y, z);

                boolean frame = w == 0 || w == 3
                        || h == 0 || h == 4;

                if (frame) {
                    block.setType(Material.OBSIDIAN);
                } else {
                    block.setType(Material.NETHER_PORTAL);

                    Orientable data = (Orientable) block.getBlockData();
                    data.setAxis(axis);
                    block.setBlockData(data);
                }
            }
        }

        player.sendMessage(
                ChatColor.GREEN + "Your Nether portal has been created!"
        );
        player.playSound(loc, Sound.BLOCK_PORTAL_TRIGGER, 1f, 1f);
    }
}
