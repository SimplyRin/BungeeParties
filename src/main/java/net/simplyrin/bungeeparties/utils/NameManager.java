package net.simplyrin.bungeeparties.utils;

import java.util.UUID;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.simplyrin.bungeeparties.Main;

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
