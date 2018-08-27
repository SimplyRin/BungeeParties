package net.simplyrin.bungeeparties;

import java.util.UUID;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.simplyrin.bungeeparties.commands.PartyCommand;
import net.simplyrin.bungeeparties.listeners.EventListener;
import net.simplyrin.bungeeparties.utils.ConfigManager;
import net.simplyrin.bungeeparties.utils.LanguageManager;
import net.simplyrin.bungeeparties.utils.MessageBuilder;
import net.simplyrin.bungeeparties.utils.NameManager;
import net.simplyrin.bungeeparties.utils.PartyManager;
import net.simplyrin.bungeeparties.utils.PlayerManager;

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
		plugin.getProxy().getPluginManager().registerListener(plugin, new EventListener(plugin));
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

}
