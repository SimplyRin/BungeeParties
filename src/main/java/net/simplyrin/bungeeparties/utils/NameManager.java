package net.simplyrin.bungeeparties.utils;

import java.util.UUID;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.simplyrin.bungeeparties.Main;

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
public class NameManager {

	private Main plugin;
	private static net.simplyrin.bungeefriends.Main bungeeFriendsInstance;

	public NameManager(Main plugin) {
		this.plugin = plugin;
	}

	public static void setBungeeFriendsInstance(net.simplyrin.bungeefriends.Main instance) {
		NameManager.bungeeFriendsInstance = instance;
	}

	public NameUtils getPlayer(ProxiedPlayer player) {
		return new NameUtils(player.getUniqueId());
	}

	public NameUtils getPlayer(String uuid) {
		return new NameUtils(UUID.fromString(uuid));
	}

	public NameUtils getPlayer(UUID uniqueId) {
		return new NameUtils(uniqueId);
	}

	public class NameUtils {

		private UUID uuid;

		public NameUtils(UUID uuid) {
			this.uuid = uuid;
		}

		public String getDisplayName() {
			if(NameManager.bungeeFriendsInstance != null) {
				return NameManager.bungeeFriendsInstance.getFriendManager().getPlayer(this.uuid).getDisplayName();
			}

			return "&7" + NameManager.this.plugin.getPlayerManager().getPlayerName(this.uuid);
		}

	}

}
