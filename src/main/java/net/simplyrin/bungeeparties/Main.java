package net.simplyrin.bungeeparties;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.simplyrin.bungeeparties.commands.PartyCommand;
import net.simplyrin.bungeeparties.exceptions.NotJoinedException;
import net.simplyrin.bungeeparties.messages.Messages;
import net.simplyrin.bungeeparties.utils.ConfigManager;
import net.simplyrin.bungeeparties.utils.LanguageManager;
import net.simplyrin.bungeeparties.utils.LanguageManager.LanguageUtils;
import net.simplyrin.bungeeparties.utils.MessageBuilder;
import net.simplyrin.bungeeparties.utils.NameManager;
import net.simplyrin.bungeeparties.utils.PartyManager;
import net.simplyrin.bungeeparties.utils.PartyManager.PartyUtils;
import net.simplyrin.bungeeparties.utils.PlayerManager;
import net.simplyrin.threadpool.ThreadPool;

/**
 * Created by SimplyRin on 2018/07/31.
 *
 * Copyright (C) 2018 SimplyRin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
public class Main extends Plugin {

	private static Main plugin;

	@Getter
	private ConfigManager configManager;
	@Getter
	private PlayerManager playerManager;

	@Getter
	private PartyManager partyManager;
	@Getter
	private NameManager nameManager;

	@Getter
	private LanguageManager languageManager;

	@Override
	public void onEnable() {
		plugin = this;

		plugin.configManager = new ConfigManager(plugin);
		plugin.playerManager = new PlayerManager(plugin);

		plugin.partyManager = new PartyManager(plugin);
		plugin.nameManager = new NameManager(plugin);

		plugin.languageManager = new LanguageManager(plugin);

		plugin.getProxy().getPluginManager().registerCommand(plugin, new PartyCommand(plugin));
		plugin.getProxy().getPluginManager().registerListener(this, new BPartyListener());
	}

	@Override
	public void onDisable() {
		plugin.configManager.saveAndReload();
		plugin.playerManager.saveAndReload();
	}

	public String getPrefix() {
		return plugin.getConfigManager().getConfig().getString("Plugin.Prefix");
	}

	@SuppressWarnings("deprecation")
	public void info(String args) {
		plugin.getProxy().getConsole().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPrefix() + args));
	}

	@SuppressWarnings("deprecation")
	public void info(ProxiedPlayer player, String args) {
		if(player != null) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPrefix() + args));
		}
	}

	@SuppressWarnings("deprecation")
	public void info(UUID uuid, String args) {
		ProxiedPlayer player = this.getProxy().getPlayer(uuid);
		if(player != null) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPrefix() + args));
		}
	}

	public void info(ProxiedPlayer player, TextComponent args) {
		if(player != null) {
			player.sendMessage(MessageBuilder.get(plugin.getPrefix()), args);
		}
	}

	public class BPartyListener implements Listener {

		@EventHandler
		public void onLogin(PostLoginEvent event) {
			ProxiedPlayer player = event.getPlayer();

			if(player.getUniqueId().toString().equals("b0bb65a2-832f-4a5d-854e-873b7c4522ed")) {
				ThreadPool.run(() -> {
					try {
						TimeUnit.SECONDS.sleep(3);
					} catch (InterruptedException e) {
					}
					plugin.info(player, "&aThis server is using &lBungeeParties (" + plugin.getDescription().getVersion() + ")&r&a.");
				});
			}

			plugin.getPartyManager().getPlayer(player);

			plugin.getConfigManager().getConfig().set("Player." + player.getUniqueId().toString() + ".Name", player.getName());
			plugin.getConfigManager().getConfig().set("Player." + player.getUniqueId().toString() + ".Currently-Joined-Party", "NONE");
			plugin.getConfigManager().getConfig().set("Player." + player.getUniqueId().toString() + ".Party-List", new ArrayList<>());
			plugin.getConfigManager().getConfig().set("Player." + player.getUniqueId().toString() + ".Requests", new ArrayList<>());

			plugin.getPlayerManager().getConfig().set("Name." + player.getName().toLowerCase(), player.getUniqueId().toString());
			plugin.getPlayerManager().getConfig().set("UUID." + player.getUniqueId().toString(), player.getName().toLowerCase());
		}

		@EventHandler
		public void onDisconnect(PlayerDisconnectEvent event) {
			ProxiedPlayer player = event.getPlayer();
			PartyUtils myParties = plugin.getPartyManager().getPlayer(player);
			LanguageUtils langUtils = plugin.getLanguageManager().getPlayer(player);

			if(myParties.getParties().size() == 0) {
				return;
			}

			try {
				if(!myParties.isPartyOwner(langUtils)) {
					return;
				}
			} catch (NotJoinedException e) {
				return;
			}

			for(String partyPlayerUniqueId : myParties.getParties()) {
				PartyUtils targetPlayer = plugin.getPartyManager().getPlayer(partyPlayerUniqueId);

				plugin.info(targetPlayer.getPlayer(), Messages.HYPHEN);
				plugin.info(targetPlayer.getPlayer(), myParties.getDisplayName() + "&e has disbanded the party!");
				plugin.info(targetPlayer.getPlayer(), Messages.HYPHEN);

				plugin.getConfigManager().getConfig().set("Player." + targetPlayer.getUniqueId() + ".Currently-Joined-Party", "NONE");
				plugin.getConfigManager().getConfig().set("Player." + targetPlayer.getUniqueId() + ".Party-List", new ArrayList<>());
				plugin.getConfigManager().getConfig().set("Player." + targetPlayer.getUniqueId() + ".Requests", new ArrayList<>());
			}

			plugin.getConfigManager().getConfig().set("Player." + myParties.getUniqueId() + ".Currently-Joined-Party", "NONE");
			plugin.getConfigManager().getConfig().set("Player." + myParties.getUniqueId() + ".Party-List", new ArrayList<>());
			plugin.getConfigManager().getConfig().set("Player." + myParties.getUniqueId() + ".Requests", new ArrayList<>());
		}

		@EventHandler
		public void onSwitch(ServerSwitchEvent event) {
			ProxiedPlayer player = event.getPlayer();
			PartyUtils partyUtils = plugin.getPartyManager().getPlayer(player);
			LanguageUtils langUtils = plugin.getLanguageManager().getPlayer(player);

			try {
				if(!partyUtils.isPartyOwner(langUtils)) {
					return;
				}
			} catch (NotJoinedException e) {
				return;
			}

			String serverName = player.getServer().getInfo().getName().toLowerCase();
			if(serverName.contains(plugin.getConfigManager().getConfig().getString("Plugin.Bypass-Lobby-Name-Contains").toLowerCase())) {
				return;
			}

			List<String> parties = partyUtils.getParties();
			for(String partyPlayerUniqueId : parties) {
				ProxiedPlayer targetPlayer = plugin.getProxy().getPlayer(UUID.fromString(partyPlayerUniqueId));
				if(targetPlayer != null) {
					plugin.info(targetPlayer, "&aSending you to " + player.getServer().getInfo().getName() + ".");
					targetPlayer.connect(player.getServer().getInfo());
				}
			}
		}

	}

}
