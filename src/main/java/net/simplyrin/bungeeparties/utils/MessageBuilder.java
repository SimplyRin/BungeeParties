package net.simplyrin.bungeeparties.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

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
public class MessageBuilder {

	public static TextComponent get(String message) {
		return new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
	}

	public static TextComponent get(String message, String command, ChatColor color, String hover, boolean bold) {
		TextComponent textComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', message));

		if(command != null) {
			textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
		}
		if(hover != null) {
			textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', hover)).create()));
		}
		if(color != null) {
			textComponent.setColor(color);
		}

		textComponent.setBold(bold);
		return textComponent;
	}

}
