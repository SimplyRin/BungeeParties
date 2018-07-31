package net.simplyrin.bungeeparties.utils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import lombok.Getter;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.bungeeparties.Main;
import net.simplyrin.config.Config;

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
 */
public class PlayerManager {

	private Main plugin;
	@Getter
	private Runnable runnable;
	@Getter
	private Configuration config;

	public PlayerManager(Main plugin) {
		this.plugin = plugin;

		this.createConfig();
		this.saveAndReload();
	}

	public UUID getPlayerUniqueId(String name) {
		String string = this.config.getString("Name." + name.toLowerCase());
		if(string.equals("NONE")) {
			return null;
		}
		if(string.length() != 36) {
			return null;
		}
		try {
			return UUID.fromString(string);
		} catch (Exception e) {
		}
		return null;
	}

	public String getPlayerName(UUID uuid) {
		return this.config.getString("UUID." + uuid.toString());
	}

	public void saveAndReload() {
		File config = new File(this.plugin.getDataFolder(), "player.yml");

		Config.saveConfig(this.config, config);
		this.config = Config.getConfig(config);
	}

	public void createConfig() {
		File folder = this.plugin.getDataFolder();
		if(!folder.exists()) {
			folder.mkdir();
		}

		File config = new File(folder, "player.yml");
		if(!config.exists()) {
			try {
				config.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			this.config = Config.getConfig(config);

			this.config.set("UUID.b0bb65a2-832f-4a5d-854e-873b7c4522ed", "SimplyRin");
			this.config.set("Name.simplyrin", "b0bb65a2-832f-4a5d-854e-873b7c4522ed");

			this.config.set("UUID.64636120-8633-4541-aa5f-412b42ddb04d", "SimplyFoxy");
			this.config.set("Name.simplyfoxy", "64636120-8633-4541-aa5f-412b42ddb04d");

			Config.saveConfig(this.config, config);
		}

		this.config = Config.getConfig(config);
		this.saveAndReload();
	}

}
