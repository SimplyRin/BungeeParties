package net.simplyrin.bungeeparties.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.simplyrin.bungeeparties.Main;
import net.simplyrin.bungeeparties.exceptions.NotJoinedException;
import net.simplyrin.bungeeparties.messages.Messages;
import net.simplyrin.bungeeparties.utils.LanguageManager.LanguageUtils;
import net.simplyrin.bungeeparties.utils.PartyManager.PartyUtils;
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
 */
public class EventListener implements Listener {

	private Main plugin;

	public EventListener(Main plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onLogin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();

		if(player.getUniqueId().toString().equals("b0bb65a2-832f-4a5d-854e-873b7c4522ed")) {
			ThreadPool.run(() -> {
				try {
					TimeUnit.SECONDS.sleep(3);
				} catch (InterruptedException e) {
				}
				this.plugin.info(player, "&aThis server is using &lBungeeParties (" + this.plugin.getDescription().getVersion() + ")&r&a.");
			});
		}

		this.plugin.getPartyManager().getPlayer(player);

		this.plugin.getConfigManager().getConfig().set("Player." + player.getUniqueId().toString() + ".Name", player.getName());
		this.plugin.getConfigManager().getConfig().set("Player." + player.getUniqueId().toString() + ".Currently-Joined-Party", "NONE");
		this.plugin.getConfigManager().getConfig().set("Player." + player.getUniqueId().toString() + ".Party-List", new ArrayList<>());
		this.plugin.getConfigManager().getConfig().set("Player." + player.getUniqueId().toString() + ".Requests", new ArrayList<>());

		this.plugin.getPlayerManager().getConfig().set("Name." + player.getName().toLowerCase(), player.getUniqueId().toString());
		this.plugin.getPlayerManager().getConfig().set("UUID." + player.getUniqueId().toString(), player.getName().toLowerCase());
	}

	@EventHandler
	public void onDisconnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		PartyUtils myParties = this.plugin.getPartyManager().getPlayer(player);
		LanguageUtils langUtils = this.plugin.getLanguageManager().getPlayer(player);

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
			PartyUtils targetPlayer = this.plugin.getPartyManager().getPlayer(partyPlayerUniqueId);

			this.plugin.info(targetPlayer.getPlayer(), Messages.HYPHEN);
			this.plugin.info(targetPlayer.getPlayer(), myParties.getDisplayName() + "&e has disbanded the party!");
			this.plugin.info(targetPlayer.getPlayer(), Messages.HYPHEN);

			this.plugin.getConfigManager().getConfig().set("Player." + targetPlayer.getUniqueId() + ".Currently-Joined-Party", "NONE");
			this.plugin.getConfigManager().getConfig().set("Player." + targetPlayer.getUniqueId() + ".Party-List", new ArrayList<>());
			this.plugin.getConfigManager().getConfig().set("Player." + targetPlayer.getUniqueId() + ".Requests", new ArrayList<>());
		}

		this.plugin.getConfigManager().getConfig().set("Player." + myParties.getUniqueId() + ".Currently-Joined-Party", "NONE");
		this.plugin.getConfigManager().getConfig().set("Player." + myParties.getUniqueId() + ".Party-List", new ArrayList<>());
		this.plugin.getConfigManager().getConfig().set("Player." + myParties.getUniqueId() + ".Requests", new ArrayList<>());
	}

	@EventHandler
	public void onSwitch(ServerSwitchEvent event) {
		ProxiedPlayer player = event.getPlayer();
		PartyUtils partyUtils = this.plugin.getPartyManager().getPlayer(player);
		LanguageUtils langUtils = this.plugin.getLanguageManager().getPlayer(player);

		try {
			if(!partyUtils.isPartyOwner(langUtils)) {
				return;
			}
		} catch (NotJoinedException e) {
			return;
		}

		String serverName = player.getServer().getInfo().getName().toLowerCase();
		if(serverName.contains(this.plugin.getConfigManager().getConfig().getString("Plugin.Bypass-Lobby-Name-Contains").toLowerCase())) {
			return;
		}

		List<String> parties = partyUtils.getParties();
		for(String partyPlayerUniqueId : parties) {
			ProxiedPlayer targetPlayer = this.plugin.getProxy().getPlayer(UUID.fromString(partyPlayerUniqueId));
			if(targetPlayer != null) {
				this.plugin.info(targetPlayer, "&aSending you to " + player.getServer().getInfo().getName() + ".");
				targetPlayer.connect(player.getServer().getInfo());
			}
		}
	}

}
