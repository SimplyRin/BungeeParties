package net.simplyrin.bungeeparties.utils;

import java.io.File;
import java.util.ArrayList;

import lombok.Getter;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.bungeeparties.Main;
import net.simplyrin.config.Config;

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
public class ConfigManager {

	private Main plugin;
	@Getter
	private Runnable runnable;
	@Getter
	private Configuration config;

	public ConfigManager(Main plugin) {
		this.plugin = plugin;

		this.createConfig();
		this.saveAndReload();
	}

	public void saveAndReload() {
		File config = new File(this.plugin.getDataFolder(), "config.yml");

		Config.saveConfig(this.config, config);
		this.config = Config.getConfig(config);
	}

	public void createConfig() {
		File folder = this.plugin.getDataFolder();
		if(!folder.exists()) {
			folder.mkdir();
		}

		File config = new File(folder, "config.yml");
		if(!config.exists()) {
			try {
				config.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}

			this.config = Config.getConfig(config);

			this.config.set("Plugin.Prefix", "&7[&cParties&7] &r");
			this.config.set("Plugin.Bypass-Lobby-Name-Contains", "lobby");

			this.config.set("Player.b0bb65a2-832f-4a5d-854e-873b7c4522ed.Name", "SimplyRin");
			this.config.set("Player.b0bb65a2-832f-4a5d-854e-873b7c4522ed.Language", "english");
			this.config.set("Player.b0bb65a2-832f-4a5d-854e-873b7c4522ed.Currently-Joined-Party", "NONE");
			this.config.set("Player.b0bb65a2-832f-4a5d-854e-873b7c4522ed.Party-List", new ArrayList<>());
			this.config.set("Player.b0bb65a2-832f-4a5d-854e-873b7c4522ed.Requests", new ArrayList<>());
			this.config.set("Player.b0bb65a2-832f-4a5d-854e-873b7c4522ed.Toggle", true);

			this.config.set("Player.64636120-8633-4541-aa5f-412b42ddb04d.Name", "SimplyFoxy");
			this.config.set("Player.64636120-8633-4541-aa5f-412b42ddb04d.Language", "english");
			this.config.set("Player.64636120-8633-4541-aa5f-412b42ddb04d.Currently-Joined-Party", "NONE");
			this.config.set("Player.64636120-8633-4541-aa5f-412b42ddb04d.Party-List", new ArrayList<>());
			this.config.set("Player.64636120-8633-4541-aa5f-412b42ddb04d.Requests", new ArrayList<>());
			this.config.set("Player.64636120-8633-4541-aa5f-412b42ddb04d.Toggle", true);

			Config.saveConfig(this.config, config);
		}

		this.config = Config.getConfig(config);

		this.resetValue("Plugin.Disable-Aliases./pc");

		this.saveAndReload();
	}

	public void resetValue(String key) {
		if (!this.config.getBoolean(key)) {
			this.config.set(key, false);
		}
	}

}
