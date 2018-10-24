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
 * Copyright (c) 2018 SimplyRin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
