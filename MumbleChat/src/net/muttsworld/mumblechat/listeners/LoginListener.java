package net.muttsworld.mumblechat.listeners;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

import net.muttsworld.mumblechat.ChatChannel;
import net.muttsworld.mumblechat.ChatChannelInfo;
import net.muttsworld.mumblechat.MumbleChat;
import net.muttsworld.mumblechat.MumbleChat.LOG_LEVELS;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class LoginListener implements Listener {

    MumbleChat plugin;
    ChatChannelInfo cc;
    String defaultChannel;
    String defaultColor;
    FileConfiguration customConfig = null;
    File customConfigFile = null;

    public void SaveItToDisk() {
        //saveCustomConfig();
        try {
            customConfig.save(customConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, e);
            //	  logger.severe(PREFIX + " error writting configurations");
            e.printStackTrace();
        }
    }

    public LoginListener(MumbleChat _plugin, ChatChannelInfo _cc) {
        plugin = _plugin;
        cc = _cc;
        for (ChatChannel c : cc.getChannelsInfo()) {
            if (c.isDefaultchannel()) {
                defaultChannel = c.getName();
                defaultColor = c.getColor();
            }
        }
        reloadCustomConfig();
    }

    @EventHandler(priority = EventPriority.LOW) // Makes your event Low priority
    void onPlayerKick(PlayerKickEvent plog) {
        if (cc.saveplayerdata) {
            PlayerLeaving(plog.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOW) // Makes your event Low priority
    void onPlayerQuit(PlayerQuitEvent plog) {
        if (cc.saveplayerdata) {
            PlayerLeaving(plog.getPlayer());
        }
    }

    void PlayerLeaving(Player pp) {

        //mama.getServer().getLogger().info("Logout.. ");

        //String curChannel;
        customConfig = getCustomConfig();
        Player pl = pp;

        Boolean nothingspecial = true;
        for (ChatChannel c : cc.getChannelsInfo()) {
            if (plugin.getMetadata(pl, "listenchannel." + c.getName(), plugin)) {

                //if they are only listening to the default and only talking on the default....
                if (!plugin.getMetadata(pl, "MumbleMute." + c.getName(), plugin) && !c.isDefaultchannel() && !c.getAutojoin() && plugin.getMetadataString(pl, "currentchannel", plugin).equalsIgnoreCase(c.getName())) {
                    nothingspecial = false;
                }

            }

        }
        if (!plugin.getMetadataString(pl, "MumbleChat.ignore", plugin).isEmpty()) {
            nothingspecial = false;
        }


        //If they are only listening to the default and autojoin channels no point in saving them.
        if (nothingspecial) {

            plugin.logme(LOG_LEVELS.DEBUG, "Player Logoff", "No special chat stuff.. not savings them");
            return;
        }

        //if(getMetadataString(p,"listenchannel".+))

        ConfigurationSection cs = customConfig.getConfigurationSection("players." + pl.getPlayerListName());
        if (cs == null) {
            //	mama.getServer().getLogger().info("Logout.. No Player Found");
            //cs = new ConfigurationSection();
            //cs = customConfig.createSection("players");
            ConfigurationSection ps = customConfig.getConfigurationSection("players");
            if (ps == null) {
                cs = customConfig.createSection("players");
            }
            cs = customConfig.createSection("players." + pl.getPlayerListName());

        }

        cs.set("default", plugin.getMetadataString(pl, "currentchannel", plugin));
        //Save the Ignores list...
        cs.set("ignores", plugin.getMetadataString(pl, "MumbleChat.ignore", plugin));


        //	mama.getServer().getLogger().info("After Section.... ");

        String strListening = "";
        String strMutes = "";
        for (ChatChannel c : cc.getChannelsInfo()) {
            if (plugin.getMetadata(pl, "listenchannel." + c.getName(), plugin)) {
                strListening += c.getName() + ",";
            }

            if (plugin.getMetadata(pl, "MumbleMute." + c.getName(), plugin)) {
                strMutes += c.getName() + ",";
            }

        }

        //	mama.getServer().getLogger().info("After Section....2 ");

        if (strListening.length() > 0) {
            strListening = strListening.substring(0, strListening.length() - 1);
        }

        //	mama.getServer().getLogger().info("After Section....2 " + strListening);

        cs.set("listen", strListening);

        if (strMutes.length() > 0) {
            strMutes = strMutes.substring(0, strMutes.length() - 1);
        }
        //	mama.getServer().getLogger().info("After Section.. " + strMutes);;

        cs.set("mutes", strMutes);

        //	mama.getServer().getLogger().info("After Section....3 ");



        Calendar currentDate = Calendar.getInstance();
        SimpleDateFormat formatter =
                new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss");
        String dateNow = formatter.format(currentDate.getTime());

        //	   mama.getServer().getLogger().info("Before Save:" + dateNow);
        cs.set("date", dateNow);

        //Do we want this Disk IO on every logout..or do we
        //just want to wait for server stop.
        //lets wait until server stops...
        // saveCustomConfig();
        // reloadCustomConfig();
    }

    ///////////////////////////////////////////
    //Function:
    //   onPlayerLogin
    //
    //  Responds to Player Login event.
    ///////////////////////////////////////////
    @EventHandler(priority = EventPriority.LOW) // Makes your event Low priority
    void onPlayerLogin(PlayerLoginEvent plog) {
        String curChannel;
        Player pl = plog.getPlayer();

        if (cc.usePexPrefix == true) {
            PermissionUser user = PermissionsEx.getUser(pl);
            //http://www.minecraftwiki.net/wiki/Classic_server_protocol#Color_Codes
            String pFormatted = cc.FormatPlayerName(user.getPrefix(), "%s", user.getSuffix());
            //pl.getPlayerListName()
            //So it shows when you login.
            //However this is bad.. as it makes who impossible....
            //pl.setDisplayName(pFormatted);

            //put player tag in metadata... this way we don't keep calling permissionex in chatlistener.
            pl.setMetadata("chatnameformat", new FixedMetadataValue(plugin, pFormatted));
        }


        if (cc.saveplayerdata) {
            customConfig = getCustomConfig();
            plugin.logme(LOG_LEVELS.DEBUG, "Player Login", "We are saving player data");

            //mama.getServer().getLogger().info("before Listen");
            ConfigurationSection cs = customConfig.getConfigurationSection("players." + pl.getPlayerListName());
            if (cs != null) {
                plugin.logme(LOG_LEVELS.DEBUG, "Player Login", "Player's data has been found");

                curChannel = cs.getString("default", defaultChannel);
                pl.setMetadata("currentchannel", new FixedMetadataValue(plugin, curChannel));

                //Get the Ignore list.. if they have one.
                String ignores = cs.getString("ignores", "");
                pl.setMetadata("MumbleChat.ignore", new FixedMetadataValue(plugin, ignores));


                plugin.logme(LOG_LEVELS.DEBUG, "Player Login", "Check for listen channels");
                //check for channels to listen too...
                String listenChannels = cs.getString("listen", "");

                plugin.logme(LOG_LEVELS.DEBUG, "Player Login", "Listenchannels:" + listenChannels);
                if (listenChannels.length() > 0) {
                    //String[] pparse = new String[2];

                    StringTokenizer st = new StringTokenizer(listenChannels, ",");
                    while (st.hasMoreTokens()) {

                        String chname = st.nextToken();
                        ChatChannel c = cc.getChannelInfo(chname);
                        plugin.logme(LOG_LEVELS.DEBUG, "Player Login", "Check for each channel:" + c.getName());
                        //Check for Channel Permission before allowing player to use channel.
                        //Incase their permissions change.
                        if (c.hasPermission()) {
                            plugin.logme(LOG_LEVELS.DEBUG, "Player Login", "Channel has permissions");
                            if (pl.isPermissionSet(c.getPermission())) {
                                pl.setMetadata("listenchannel." + chname, new FixedMetadataValue(plugin, true));
                            }
                        } else {
                            pl.setMetadata("listenchannel." + chname, new FixedMetadataValue(plugin, true));
                        }
                    }
                } else //if no channel is available to listen on... set it to default... they should listen on something.
                {
                    pl.sendMessage("You have no channels to listen to... setting listen to " + defaultChannel);
                    pl.sendMessage("Check /chlist for a list of available channels.");
                    pl.setMetadata("listenchannel." + defaultChannel, new FixedMetadataValue(plugin, true));
                }
                //	mama.getServer().getLogger().info("before Mutes");
                String muteChannels = cs.getString("mutes", "");
                if (muteChannels.length() > 0) {
                    StringTokenizer st = new StringTokenizer(muteChannels, ",");
                    while (st.hasMoreTokens()) {
                        pl.setMetadata("MumbleMute." + st.nextToken(), new FixedMetadataValue(plugin, true));

                    }
                }




            } else {
                //mama.getServer().getLogger().info("No Player Found");
                curChannel = defaultChannel;
                pl.setMetadata("currentchannel", new FixedMetadataValue(plugin, defaultChannel));
                pl.setMetadata("listenchannel." + defaultChannel, new FixedMetadataValue(plugin, true));
            }

        } else {
            curChannel = defaultChannel;
            pl.setMetadata("currentchannel", new FixedMetadataValue(plugin, defaultChannel));
            pl.setMetadata("listenchannel." + defaultChannel, new FixedMetadataValue(plugin, true));
        }

        //reset quick talk
        pl.setMetadata("insertchannel", new FixedMetadataValue(plugin, "NONE"));

        //Set AutoJoins up.. just make sure they are listening
        List<String> autolist = cc.getAutojoinList();
        if (autolist.size() > 0) {
            for (String s : autolist) {
                pl.setMetadata("listenchannel." + s, new FixedMetadataValue(plugin, true));
            }
        }

        String curColor = defaultColor;
        for (ChatChannel c : cc.getChannelsInfo()) {
            if (c.getName().equalsIgnoreCase(curChannel)) {
                curColor = c.getColor();
            }
        }

        String format = ChatColor.valueOf(curColor.toUpperCase()) + "[" + curChannel + "]";
        pl.setMetadata("format", new FixedMetadataValue(plugin, format));


        //====================================================================================
        // ---  Get Permissions for Special Commands here -----------------------------------
        //====================================================================================
        if (pl.isPermissionSet(cc.mutepermissions)) //pl.hasPermission(cc.mutepermissions))
        {
            plugin.getServer().getLogger().info("[" + plugin.getName() + "] can Mute permissions given...");
            pl.setMetadata("mumblechat.canmute", new FixedMetadataValue(plugin, true));
        }

        ////////////////////////////////////////////////////////////////////////////////////////
        //FUTURE FORCE CHANNEL CODE
        ///////////////////////////////////////////////////////////////////////////////////////
        if (pl.isPermissionSet(cc.forcepermissions)) {
            plugin.getServer().getLogger().info("[" + plugin.getName() + "] can Force permissions given...");
            pl.setMetadata("mumblechat.canforce", new FixedMetadataValue(plugin, true));
        }

        for (ChatChannel c : cc.getChannelsInfo()) {
            //	mama.getServer().getLogger().info("Find Stuff  " + c.getName() + " " + c.getPermission());
            if (c.hasPermission()) {
                //	mama.getServer().getLogger().info("Perms Exist!" + c.getPermission());

                if (pl.isPermissionSet(c.getPermission())) {
                    //mama.getServer().getLogger().info("And I can use them!!!" + c.getPermission());
                    pl.setMetadata(c.getPermission(), new FixedMetadataValue(plugin, true));
                } else {
                    pl.setMetadata(c.getPermission(), new FixedMetadataValue(plugin, false));
                }
            }
        }




        //mama.getServer().getLogger().info("End the Login Event");
    }

    public void reloadCustomConfig() {

        //TODO: Consider that this is a hardcoded filename... ick.
        if (customConfigFile == null) {
            customConfigFile = new File(plugin.getDataFolder().getAbsolutePath(), "PlayerData.yml");
        }
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);

        /*  // Look for defaults in the jar
        InputStream defConfigStream = mama.getResource("PlayerData.yml");
        if (defConfigStream != null) {
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
        customConfig.setDefaults(defConfig);
        }*/
    }

    public FileConfiguration getCustomConfig() {
        if (customConfig == null) {
            this.reloadCustomConfig();
        }
        return customConfig;
    }

    public void saveCustomConfig() {
        if (customConfig == null || customConfigFile == null) {
            return;
        }
        try {
            getCustomConfig().save(customConfigFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
        }
    }
}
