package net.simplyrin.bungeeparties.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.UUID;

import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.bungeeparties.Main;
import net.simplyrin.config.Config;

/**
 * Created by SimplyRin on 2018/08/27.
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
public class LanguageManager {

	private Main plugin;

	private HashMap<String, Configuration> configMap;

	public LanguageManager(Main plugin) {
		this.plugin = plugin;
		this.configMap = new HashMap<>();

		File folder = LanguageManager.this.plugin.getDataFolder();
		if(!folder.exists()) {
			folder.mkdir();
		}

		File languageFolder = new File(folder, "Language");
		if(!languageFolder.exists()) {
			languageFolder.mkdir();
		}

		String[] langs = { "english", "japanese", "chinese", "arabic" };
		for(String lang : langs) {
			File languageFile = new File(languageFolder, lang + ".yml");
			if(!languageFile.exists()) {
				try {
					InputStream inputStream = LanguageManager.this.plugin.getResourceAsStream(lang + ".yml");
					FileOutputStream outputStream = new FileOutputStream(new File(languageFolder, lang + ".yml"));
					ByteStreams.copy(inputStream, outputStream);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public LanguageUtils getPlayer(ProxiedPlayer player) {
		return new LanguageUtils(player.getUniqueId());
	}

	public LanguageUtils getPlayer(String uuid) {
		return new LanguageUtils(UUID.fromString(uuid));
	}

	public LanguageUtils getPlayer(UUID uniqueId) {
		return new LanguageUtils(uniqueId);
	}

	public class LanguageUtils {

		private UUID uuid;

		public LanguageUtils(UUID uuid) {
			this.uuid = uuid;

			Object lang = LanguageManager.this.plugin.getConfigManager().getConfig().get("Player." + this.uuid.toString() + ".Language");
			if(lang == null || lang.equals("")) {
				LanguageManager.this.plugin.getConfigManager().getConfig().set("Player." + this.uuid.toString() + ".Language", "english");
				LanguageManager.this.configMap.put("english", Config.getConfig(this.getFile("english")));
			}
		}

		public String getLanguage() {
			String key = LanguageManager.this.plugin.getConfigManager().getConfig().getString("Player." + this.uuid.toString() + ".Language");
			if(key == null || key.equals("")) {
				return "english";
			}
			return key.substring(0, 1).toUpperCase() + key.substring(1, key.length());
		}

		public void setLanguage(String key) {
			LanguageManager.this.plugin.getConfigManager().getConfig().set("Player." + this.uuid.toString() + ".Language", key);
		}

		public String getString(String configKey) {
			Configuration config = LanguageManager.this.configMap.get(this.getLanguage());

			if(config == null) {
				File file = new File(this.getLanguagesFolder(), this.getLanguage().toLowerCase() + ".yml");
				LanguageManager.this.configMap.put(this.getLanguage(), Config.getConfig(file));
			}

			config = LanguageManager.this.configMap.get(this.getLanguage());
			return config.getString(configKey);
		}

		public File getLanguagesFolder() {
			File folder = LanguageManager.this.plugin.getDataFolder();
			if(!folder.exists()) {
				folder.mkdir();
			}

			File languageFolder = new File(folder, "Language");
			if(!languageFolder.exists()) {
				languageFolder.mkdir();
			}

			return languageFolder;
		}

		public File getFile() {
			return this.getFile(this.getLanguage());
		}

		public File getFile(String key) {
			return new File(this.getLanguagesFolder(), key.toLowerCase() + ".yml");
		}

	}

}
