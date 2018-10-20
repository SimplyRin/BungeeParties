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
import net.simplyrin.bungeeparties.commands.alias.PCCommand;
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
		this.configManager = new ConfigManager(this);
		this.playerManager = new PlayerManager(this);

		this.partyManager = new PartyManager(this);
		this.nameManager = new NameManager(this);

		this.languageManager = new LanguageManager(this);

		if (!this.configManager.getConfig().getBoolean("this.Disable-Aliases./pc")) {
			this.getProxy().getPluginManager().registerCommand(this, new PCCommand(this));
		}

		this.getProxy().getPluginManager().registerCommand(this, new PartyCommand(this));
		this.getProxy().getPluginManager().registerListener(this, new BPartyListener());
	}

	@Override
	public void onDisable() {
		this.configManager.saveAndReload();
		this.playerManager.saveAndReload();
	}

	public String getPrefix() {
		return this.getConfigManager().getConfig().getString("Plugin.Prefix");
	}

	@SuppressWarnings("deprecation")
	public void info(String args) {
		this.getProxy().getConsole().sendMessage(ChatColor.translateAlternateColorCodes('&', this.getPrefix() + args));
	}

	@SuppressWarnings("deprecation")
	public void info(ProxiedPlayer player, String args) {
		if(player != null) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getPrefix() + args));
		}
	}

	@SuppressWarnings("deprecation")
	public void info(UUID uuid, String args) {
		ProxiedPlayer player = this.getProxy().getPlayer(uuid);
		if(player != null) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getPrefix() + args));
		}
	}

	public void info(ProxiedPlayer player, TextComponent args) {
		if(player != null) {
			player.sendMessage(MessageBuilder.get(this.getPrefix()), args);
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
					Main.this.info(player, "&aThis server is using &lBungeeParties (" + Main.this.getDescription().getVersion() + ")&r&a.");
				});
			}

			Main.this.getPartyManager().getPlayer(player);

			Main.this.getConfigManager().getConfig().set("Player." + player.getUniqueId().toString() + ".Name", player.getName());
			Main.this.getConfigManager().getConfig().set("Player." + player.getUniqueId().toString() + ".Currently-Joined-Party", "NONE");
			Main.this.getConfigManager().getConfig().set("Player." + player.getUniqueId().toString() + ".Party-List", new ArrayList<>());
			Main.this.getConfigManager().getConfig().set("Player." + player.getUniqueId().toString() + ".Requests", new ArrayList<>());

			Main.this.getPlayerManager().getConfig().set("Name." + player.getName().toLowerCase(), player.getUniqueId().toString());
			Main.this.getPlayerManager().getConfig().set("UUID." + player.getUniqueId().toString(), player.getName().toLowerCase());
		}

		@EventHandler
		public void onDisconnect(PlayerDisconnectEvent event) {
			ProxiedPlayer player = event.getPlayer();
			PartyUtils myParties = Main.this.getPartyManager().getPlayer(player);
			LanguageUtils langUtils = Main.this.getLanguageManager().getPlayer(player);

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
				PartyUtils targetPlayer = Main.this.getPartyManager().getPlayer(partyPlayerUniqueId);

				Main.this.info(targetPlayer.getPlayer(), Messages.HYPHEN);
				Main.this.info(targetPlayer.getPlayer(), myParties.getDisplayName() + "&e has disbanded the party!");
				Main.this.info(targetPlayer.getPlayer(), Messages.HYPHEN);

				Main.this.getConfigManager().getConfig().set("Player." + targetPlayer.getUniqueId() + ".Currently-Joined-Party", "NONE");
				Main.this.getConfigManager().getConfig().set("Player." + targetPlayer.getUniqueId() + ".Party-List", new ArrayList<>());
				Main.this.getConfigManager().getConfig().set("Player." + targetPlayer.getUniqueId() + ".Requests", new ArrayList<>());
			}

			Main.this.getConfigManager().getConfig().set("Player." + myParties.getUniqueId() + ".Currently-Joined-Party", "NONE");
			Main.this.getConfigManager().getConfig().set("Player." + myParties.getUniqueId() + ".Party-List", new ArrayList<>());
			Main.this.getConfigManager().getConfig().set("Player." + myParties.getUniqueId() + ".Requests", new ArrayList<>());
		}

		@EventHandler
		public void onSwitch(ServerSwitchEvent event) {
			ProxiedPlayer player = event.getPlayer();
			PartyUtils partyUtils = Main.this.getPartyManager().getPlayer(player);
			LanguageUtils langUtils = Main.this.getLanguageManager().getPlayer(player);

			try {
				if(!partyUtils.isPartyOwner(langUtils)) {
					return;
				}
			} catch (NotJoinedException e) {
				return;
			}

			String serverName = player.getServer().getInfo().getName().toLowerCase();
			if(serverName.contains(Main.this.getConfigManager().getConfig().getString("Plugin.Bypass-Lobby-Name-Contains").toLowerCase())) {
				return;
			}

			List<String> parties = partyUtils.getParties();
			for(String partyPlayerUniqueId : parties) {
				ProxiedPlayer targetPlayer = Main.this.getProxy().getPlayer(UUID.fromString(partyPlayerUniqueId));
				if(targetPlayer != null) {
					Main.this.info(targetPlayer, "&aSending you to " + player.getServer().getInfo().getName() + ".");
					targetPlayer.connect(player.getServer().getInfo());
				}
			}
		}

	}

}
