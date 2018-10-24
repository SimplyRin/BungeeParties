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
