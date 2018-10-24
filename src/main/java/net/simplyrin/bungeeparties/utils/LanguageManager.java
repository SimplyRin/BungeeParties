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
