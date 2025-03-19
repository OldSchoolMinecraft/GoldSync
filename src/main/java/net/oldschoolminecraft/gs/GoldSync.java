package net.oldschoolminecraft.gs;

import com.earth2me.essentials.Essentials;
import com.johnymuffin.discordcore.DiscordCore;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.io.File;

public class GoldSync extends JavaPlugin
{
    private GSConfig config;
    public PermissionsEx pexHandle;
    public DiscordCore dbcHandle;
    public Essentials essHandle;

    @Override
    public void onEnable()
    {
        pexHandle = (PermissionsEx) getServer().getPluginManager().getPlugin("PermissionsEx");
        dbcHandle = (DiscordCore) getServer().getPluginManager().getPlugin("DiscordCore");
        essHandle = (Essentials) getServer().getPluginManager().getPlugin("Essentials");

        config = new GSConfig(new File(getDataFolder(), "config.yml"));

        System.out.println("GoldSync enabled");
    }

    @Override
    public void onDisable()
    {
        System.out.println("GoldSync disabled");
    }

    public GSConfig getConfig()
    {
        return config;
    }
}
