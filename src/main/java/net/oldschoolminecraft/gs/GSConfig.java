package net.oldschoolminecraft.gs;

import org.bukkit.util.config.Configuration;

import java.io.File;

public class GSConfig extends Configuration
{
    public GSConfig(File file)
    {
        super(file);
        reload();
    }

    public void reload()
    {
        load();
        write();
        save();
    }

    public void write()
    {
        generateConfigOption("db.host", "localhost");
        generateConfigOption("db.port", 3306);
        generateConfigOption("db.mainDatabase", "goldsync");
        generateConfigOption("db.bridgecordDatabase", "bridgecord");
        generateConfigOption("db.username", "root");
        generateConfigOption("db.password", "password");

        generateConfigOption("discord.guildID", "GUILD_ID");
    }

    private void generateConfigOption(String key, Object defaultValue)
    {
        if (this.getProperty(key) == null) this.setProperty(key, defaultValue);
        final Object value = this.getProperty(key);
        this.removeProperty(key);
        this.setProperty(key, value);
    }

    public Object getConfigOption(String key)
    {
        return this.getProperty(key);
    }

    public Object getConfigOption(String key, Object defaultValue)
    {
        Object value = getConfigOption(key);
        if (value == null) value = defaultValue;
        return value;
    }
}
