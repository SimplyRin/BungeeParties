package net.simplyrin.bungeeparties.commands.alias;

import java.util.List;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.simplyrin.bungeeparties.Main;
import net.simplyrin.bungeeparties.exceptions.NotJoinedException;
import net.simplyrin.bungeeparties.messages.Messages;
import net.simplyrin.bungeeparties.utils.LanguageManager.LanguageUtils;
import net.simplyrin.bungeeparties.utils.PartyManager.PartyUtils;

/**
 * Created by SimplyRin on 2018/10/20.
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
public class PCCommand extends Command {

	private Main plugin;

	public PCCommand(Main plugin) {
		super("pc");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!(sender instanceof ProxiedPlayer)) {
			this.plugin.info(Messages.INGAME_ONLY);
			return;
		}

		ProxiedPlayer player = (ProxiedPlayer) sender;
		PartyUtils myParties = this.plugin.getPartyManager().getPlayer(player);
		LanguageUtils langUtils = this.plugin.getLanguageManager().getPlayer(player);

		if (args.length > 0) {
			String message = "";
			for (int i = 0; i < args.length; i++) {
				message = message + args[i] + " ";
			}

			PartyUtils partyLeader;
			try {
				partyLeader = myParties.getPartyLeader(langUtils);
			} catch (NotJoinedException e) {
				return;
			}

			List<String> parties = partyLeader.getParties();

			LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(partyLeader.getPlayer());
			this.plugin.info(partyLeader.getPlayer(), targetLangUtils.getString("Chat.Prefix") + " " + myParties.getDisplayName() + "&f: " + message);
			for(String partyPlayerUniqueId : parties) {
				PartyUtils member = this.plugin.getPartyManager().getPlayer(partyPlayerUniqueId);
				targetLangUtils = this.plugin.getLanguageManager().getPlayer(member.getPlayer());
				this.plugin.info(member.getPlayer(), targetLangUtils.getString("Chat.Prefix") + " " + myParties.getDisplayName() + "&f: " + message);
			}
			return;
		}

		this.plugin.info(player, Messages.HYPHEN);
		this.plugin.info(player, langUtils.getString("Chat.Usage"));
		this.plugin.info(player, Messages.HYPHEN);
		return;
	}

}
