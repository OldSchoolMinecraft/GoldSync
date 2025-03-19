package net.oldschoolminecraft.gs;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionBackend;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Objects;

public class DBPollThread
{
    private GoldSync plugin;
    private HashMap<String, BasicDataSource> dataSources = new HashMap<>();

    public DBPollThread(GoldSync plugin)
    {
        this.plugin = plugin;


    }

    public void run()
    {
        while (plugin.isEnabled())
        {
            try
            {
                Connection gsDBConnection = getConnection(plugin.getConfig().getString("db.mainDatabase"));
                Connection bcordDBConnection = getConnection(plugin.getConfig().getString("db.bridgecordDatabase"));

                // poll rank_notifs table for new entries, and send to function for processing
                processRanks(gsDBConnection);
                processEco(gsDBConnection);
                processDiscordRoles(bcordDBConnection);

                // poll eco_rewards table for new entries, and send to function for processing
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    private void processRanks(Connection connection) throws Exception
    {
        if (connection != null)
        {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM rank_notifs");
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
            {
                // process new entry
                String username = rs.getString("username");
                String rankName = rs.getString("rankName");
                boolean removal = rs.getBoolean("removal");

                if (removal)
                {
                    // remove rank from user
                    PermissionsEx.getPermissionManager().getUser(username).removeGroup(rankName);

                    Player player = plugin.getServer().getPlayer(username);
                    if (player != null) player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7You have lost the &a" + rankName + " &7rank!"));

                    // remove record
                    rs.deleteRow();
                } else {
                    // add rank to user
                    PermissionsEx.getPermissionManager().getUser(username).addGroup(rankName);

                    Player player = plugin.getServer().getPlayer(username);
                    if (player != null) player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7You have received the &a" + rankName + " &7rank!"));

                    // remove record
                    rs.deleteRow();
                }
            }
            connection.close();
        }
    }

    private void processEco(Connection connection) throws Exception
    {
        if (connection != null)
        {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM eco_rewards");
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
            {
                // process new entry
                String username = rs.getString("username");
                int amount = rs.getInt("amount");
                String reason = rs.getString("reason");

                // add amount to user's balance
                plugin.essHandle.getUser(username).giveMoney(amount);

                Player player = plugin.getServer().getPlayer(username);
                if (player != null)
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7You have received &a$" + amount + " &7for: &e" + reason));

                // remove record
                rs.deleteRow();
            }
            connection.close();
        }
    }

    private void processDiscordRoles(Connection connection) throws Exception
    {
        if (connection != null)
        {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM disc_role_notifs");
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
            {
                // process new entry
                String username = rs.getString("username");
                String roleID = rs.getString("role");
                boolean removal = rs.getBoolean("removal");

                // we will need to check the link_data table to see if they have a discord ID linked
                String discordID = getLinkedDiscordID(connection, username);

                if (discordID == null) // not linked, can't assign role
                    continue;

                // add or remove role from user
                User discordUser = plugin.dbcHandle.getDiscordBot().jda.getUserById(discordID);
                Guild guild = plugin.dbcHandle.getDiscordBot().jda.getGuildById(plugin.getConfig().getString("discord.guildID"));
                Member member = Objects.requireNonNull(guild).getMember(Objects.requireNonNull(discordUser));

                if (removal) guild.removeRoleFromMember(Objects.requireNonNull(member), Objects.requireNonNull(guild.getRoleById(roleID))).queue();
                else guild.addRoleToMember(Objects.requireNonNull(member), Objects.requireNonNull(guild.getRoleById(roleID))).queue();

                // remove record
                rs.deleteRow();
            }
        }
    }

    private String getLinkedDiscordID(Connection connection, String username)
    {
        if (connection != null)
        {
            try
            {
                PreparedStatement stmt = connection.prepareStatement("SELECT * FROM link_data WHERE username = ?");
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next())
                {
                    return rs.getString("discord_id");
                }
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
            }
        }
        return null;
    }

    private BasicDataSource getPool(String database)
    {
        if (dataSources.containsKey(database))
            return dataSources.get(database);

        // initialize DBCP2 connection pool
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl(String.format("jdbc:mysql://%s:%s/%s", plugin.getConfig().getString("db.host"), plugin.getConfig().getInt("db.port", 3306), database));
        dataSource.setUsername(plugin.getConfig().getString("db.username"));
        dataSource.setPassword(plugin.getConfig().getString("db.password"));

        dataSources.put(database, dataSource);
        return dataSource;
    }

    private Connection getConnection(String database)
    {
        try
        {
            return getPool(database).getConnection();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return null;
        }
    }
}
