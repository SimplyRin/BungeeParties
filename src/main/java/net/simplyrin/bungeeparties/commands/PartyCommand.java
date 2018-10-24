package net.simplyrin.bungeeparties.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.bungeefriends.utils.MessageBuilder;
import net.simplyrin.bungeeparties.Main;
import net.simplyrin.bungeeparties.exceptions.NotInvitedException;
import net.simplyrin.bungeeparties.exceptions.NotJoinedException;
import net.simplyrin.bungeeparties.messages.Messages;
import net.simplyrin.bungeeparties.messages.Permissions;
import net.simplyrin.bungeeparties.utils.LanguageManager.LanguageUtils;
import net.simplyrin.bungeeparties.utils.PartyManager.PartyUtils;
import net.simplyrin.config.Config;
import net.simplyrin.threadpool.ThreadPool;

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
public class PartyCommand extends Command {

	private Main plugin;

	public PartyCommand(Main plugin) {
		super("party", null, "p");
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

		this.plugin.getPlayerManager().getConfig().set("Name." + player.getName().toLowerCase(), player.getUniqueId().toString());
		this.plugin.getPlayerManager().getConfig().set("UUID." + player.getUniqueId().toString(), player.getName().toLowerCase());

		if(!player.hasPermission(Permissions.MAIN)) {
			this.plugin.info(player, langUtils.getString(Messages.NO_PERMISSION));
			return;
		}

		if(args.length > 0) {
			if(args[0].equalsIgnoreCase("invite")) {
				if(args.length > 1) {
					this.invite(player, myParties, args[1], langUtils);
					return;
				}
				this.plugin.info(player, Messages.HYPHEN);
				this.plugin.info(player, langUtils.getString("Invite.Usage"));
				this.plugin.info(player, Messages.HYPHEN);
				return;
			}

			if(args[0].equalsIgnoreCase("leave")) {
				if(this.plugin.getConfigManager().getConfig().getString("Player." + myParties.getPlayer().getUniqueId() + ".Currently-Joined-Party").equals("NONE")) {
					this.plugin.info(player, Messages.HYPHEN);
					this.plugin.info(player, langUtils.getString("No-Joined-The-Party"));
					this.plugin.info(player, Messages.HYPHEN);
					return;
				}

				PartyUtils partyLeader = myParties.leaveCurrentParty(langUtils);

				this.plugin.info(player, Messages.HYPHEN);
				this.plugin.info(player, langUtils.getString("Leave.You-Left"));
				this.plugin.info(player, Messages.HYPHEN);

				if(myParties.getParties().size() > 0) {
					LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(partyLeader.getPlayer());
					this.plugin.info(partyLeader.getPlayer(), Messages.HYPHEN);
					this.plugin.info(partyLeader.getPlayer(), langUtils.getString("Leave.Player-Left").replace("%displayName", myParties.getDisplayName()));
					this.plugin.info(partyLeader.getPlayer(), Messages.HYPHEN);

					for(String partyPlayerUniqueId : myParties.getParties()) {
						ProxiedPlayer partyPlayer = this.plugin.getProxy().getPlayer(partyPlayerUniqueId);
						if (partyPlayer != null) {
							targetLangUtils = this.plugin.getLanguageManager().getPlayer(partyPlayer);

							this.plugin.info(partyPlayer, Messages.HYPHEN);
							this.plugin.info(partyPlayer, targetLangUtils.getString("Leave.Player-Left").replace("%displayName", myParties.getDisplayName()));
							this.plugin.info(partyPlayer, Messages.HYPHEN);
						}
					}
				}
				return;
			}

			if(args[0].equalsIgnoreCase("list")) {
				if(this.plugin.getConfigManager().getConfig().getString("Player." + myParties.getPlayer().getUniqueId() + ".Currently-Joined-Party").equals("NONE")) {
					this.plugin.info(player, Messages.HYPHEN);
					this.plugin.info(player, langUtils.getString("No-Joined-The-Party"));
					this.plugin.info(player, Messages.HYPHEN);
					return;
				}

				PartyUtils partyLeader;
				try {
					partyLeader = myParties.getPartyLeader(langUtils);
				} catch (NotJoinedException e) {
					return;
				}

				List<String> parties = partyLeader.getParties();

				String raw = "";
				raw += partyLeader.getDisplayName() + (partyLeader.getPlayer() != null ? " &a● " : " &c● ");

				for(String partyPlayerUniqueId : parties) {
					PartyUtils member = this.plugin.getPartyManager().getPlayer(partyPlayerUniqueId);
					raw += member.getDisplayName() + (member.getPlayer() != null ? " &a● " : " &c● ");
				}

				this.plugin.info(player, Messages.HYPHEN);
				this.plugin.info(player, langUtils.getString("List.Party-List").replace("%size", String.valueOf(parties.size() + 1)) + " " + raw);
				this.plugin.info(player, Messages.HYPHEN);
				return;
			}

			if(args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("kick")) {
				if(args.length > 1) {
					if(this.plugin.getConfigManager().getConfig().getString("Player." + myParties.getPlayer().getUniqueId() + ".Currently-Joined-Party").equals("NONE")) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, langUtils.getString("No-Joined-The-Party"));
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}

					UUID target = this.plugin.getPlayerManager().getPlayerUniqueId(args[1]);
					if(target == null) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args[1]));
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}
					PartyUtils targetParties = this.plugin.getPartyManager().getPlayer(target);

					try {
						myParties.remove(target, langUtils);
					} catch (NotJoinedException e) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, e.getMessage());
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}

					this.plugin.info(player, Messages.HYPHEN);
					this.plugin.info(player, targetParties.getDisplayName() + " &ahas been removed from your party!");
					this.plugin.info(player, Messages.HYPHEN);

					if(myParties.getParties().size() > 0) {
						for(String partyPlayerUniqueId : myParties.getParties()) {
							ProxiedPlayer partyPlayer = this.plugin.getProxy().getPlayer(partyPlayerUniqueId);
							this.plugin.info(partyPlayer, Messages.HYPHEN);
							this.plugin.info(partyPlayer, langUtils.getString("Remove.Member-Removed").replace("%targetDisplayName", targetParties.getDisplayName()));
							this.plugin.info(partyPlayer, Messages.HYPHEN);
						}
					}

					this.plugin.info(target, Messages.HYPHEN);
					this.plugin.info(target, langUtils.getString("Remove.Me-Removed").replace("%displayName", myParties.getDisplayName()));
					this.plugin.info(target, Messages.HYPHEN);
					return;
				}
				this.plugin.info(player, Messages.HYPHEN);
				this.plugin.info(player, langUtils.getString("Remove.Usage"));
				this.plugin.info(player, Messages.HYPHEN);
				return;
			}

			if(args[0].equalsIgnoreCase("warp")) {
				if(this.plugin.getConfigManager().getConfig().getString("Player." + myParties.getPlayer().getUniqueId() + ".Currently-Joined-Party").equals("NONE")) {
					this.plugin.info(player, Messages.HYPHEN);
					this.plugin.info(player, langUtils.getString("No-Joined-The-Party"));
					this.plugin.info(player, Messages.HYPHEN);
					return;
				}

				this.plugin.info(player, Messages.HYPHEN);
				this.plugin.info(player, langUtils.getString("Warp.Summon").replace("%displayName", myParties.getDisplayName()));
				this.plugin.info(player, Messages.HYPHEN);

				for(String partyPlayerUniqueId : myParties.getParties()) {
					ProxiedPlayer targetPlayer = this.plugin.getProxy().getPlayer(UUID.fromString(partyPlayerUniqueId));
					if(targetPlayer != null) {
						targetPlayer.connect(myParties.getPlayer().getServer().getInfo());
					}

					LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(targetPlayer);
					this.plugin.info(targetPlayer, Messages.HYPHEN);
					this.plugin.info(targetPlayer, targetLangUtils.getString("Warp.Summon").replace("%displayName", myParties.getDisplayName()));
					this.plugin.info(targetPlayer, Messages.HYPHEN);
				}
				return;
			}

			if(args[0].equalsIgnoreCase("accept")) {
				if(args.length > 1) {
					UUID target = this.plugin.getPlayerManager().getPlayerUniqueId(args[1]);
					if(target == null) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args[1]));
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}
					PartyUtils targetParties = this.plugin.getPartyManager().getPlayer(target);

					try {
						targetParties.removeRequest(player, langUtils);
					} catch (NotInvitedException e) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, e.getMessage());
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}

					this.plugin.info(player, Messages.HYPHEN);
					this.plugin.info(player, langUtils.getString("Accept.You-Joined").replace("%targetDisplayName", targetParties.getDisplayName()));
					this.plugin.info(player, Messages.HYPHEN);

					LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(targetParties.getPlayer());
					this.plugin.info(targetParties.getPlayer(), Messages.HYPHEN);
					this.plugin.info(targetParties.getPlayer(), targetLangUtils.getString("Accept.Joined").replace("%displayName", myParties.getDisplayName()));
					this.plugin.info(targetParties.getPlayer(), Messages.HYPHEN);

					for(String partyPlayerUniqueId : targetParties.getParties()) {
						PartyUtils targetPlayer = this.plugin.getPartyManager().getPlayer(partyPlayerUniqueId);
						targetLangUtils = this.plugin.getLanguageManager().getPlayer(targetPlayer.getPlayer());

						this.plugin.info(targetPlayer.getPlayer(), Messages.HYPHEN);
						this.plugin.info(targetPlayer.getPlayer(), targetLangUtils.getString("Accept.Joined").replace("%displayName", myParties.getDisplayName()));
						this.plugin.info(targetPlayer.getPlayer(), Messages.HYPHEN);
					}

					try {
						targetParties.add(player, langUtils);
					} catch (Exception e) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, "&c" + e.getMessage());
						this.plugin.info(player, Messages.HYPHEN);
					}
					return;
				}
				this.plugin.info(player, Messages.HYPHEN);
				this.plugin.info(player, langUtils.getString("Accept.Usage"));
				this.plugin.info(player, Messages.HYPHEN);
				return;
			}

			if(args[0].equalsIgnoreCase("toggle")) {
				boolean bool = myParties.isEnabledReceiveRequest();
				if(bool) {
					myParties.setEnabledReceiveRequest(false);
					this.plugin.info(player, Messages.HYPHEN);
					this.plugin.info(player, langUtils.getString("Toggle.Disabled"));
					this.plugin.info(player, Messages.HYPHEN);
					return;
				}
				myParties.setEnabledReceiveRequest(true);
				this.plugin.info(player, Messages.HYPHEN);
				this.plugin.info(player, langUtils.getString("Toggle.Enabled"));
				this.plugin.info(player, Messages.HYPHEN);
				return;
			}

			if(args[0].equalsIgnoreCase("disband")) {
				try {
					if(!myParties.isPartyOwner(langUtils)) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, langUtils.getString("Disband.Must-Leader"));
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}
				} catch (NotJoinedException e) {
					this.plugin.info(player, Messages.HYPHEN);
					this.plugin.info(player, "&c" + e.getMessage());
					this.plugin.info(player, Messages.HYPHEN);
					return;
				}

				this.plugin.info(player, Messages.HYPHEN);
				this.plugin.info(player, langUtils.getString("Disband.Disbanded").replace("%displayName", myParties.getDisplayName()));
				this.plugin.info(player, Messages.HYPHEN);

				for(String partyPlayerUniqueId : myParties.getParties()) {
					PartyUtils targetPlayer = this.plugin.getPartyManager().getPlayer(partyPlayerUniqueId);
					if (targetPlayer != null) {
						LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(targetPlayer.getPlayer());

						this.plugin.info(targetPlayer.getPlayer(), Messages.HYPHEN);
						this.plugin.info(targetPlayer.getPlayer(), targetLangUtils.getString("Disband.Disbanded").replace("%displayName", myParties.getDisplayName()));
						this.plugin.info(targetPlayer.getPlayer(), Messages.HYPHEN);

						this.plugin.getConfigManager().getConfig().set("Player." + targetPlayer.getUniqueId() + ".Currently-Joined-Party", "NONE");
						this.plugin.getConfigManager().getConfig().set("Player." + targetPlayer.getUniqueId() + ".Party-List", new ArrayList<>());
						this.plugin.getConfigManager().getConfig().set("Player." + targetPlayer.getUniqueId() + ".Requests", new ArrayList<>());
					}
				}

				this.plugin.getConfigManager().getConfig().set("Player." + myParties.getUniqueId() + ".Currently-Joined-Party", "NONE");
				this.plugin.getConfigManager().getConfig().set("Player." + myParties.getUniqueId() + ".Party-List", new ArrayList<>());
				this.plugin.getConfigManager().getConfig().set("Player." + myParties.getUniqueId() + ".Requests", new ArrayList<>());
				return;
			}

			if(args[0].equalsIgnoreCase("help")) {
				this.printHelp(player, langUtils);
				return;
			}

			if(args[0].equalsIgnoreCase("chat")) {
				if(args.length > 1) {
					String message = "";
					for (int i = 1; i < args.length; i++) {
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

			if(args[0].equalsIgnoreCase("lang") || args[0].equalsIgnoreCase("language")) {
				File folder = this.plugin.getDataFolder();
				if(!folder.exists()) {
					folder.mkdir();
				}

				File languageFolder = new File(folder, "Language");
				if(!languageFolder.exists()) {
					languageFolder.mkdir();
				}

				List<String> availableList = new ArrayList<>();
				String available = "";
				File[] languages = languageFolder.listFiles();
				for(File languageFile : languages) {
					Configuration langConfig = Config.getConfig(languageFile);
					if(langConfig.getString("Language").length() > 1) {
						availableList.add(languageFile.getName().toLowerCase().replace(".yml", ""));
						available += langConfig.getString("Language") + ",";
					}
				}

				if(args.length > 1) {
					String lang = args[1];
					if(availableList.contains(lang.toLowerCase())) {
						langUtils.setLanguage(lang.toLowerCase());
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, langUtils.getString("Lang.Update").replace("%lang", langUtils.getLanguage()));
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}
				}

				this.plugin.info(player, Messages.HYPHEN);
				this.plugin.info(player, langUtils.getString("Lang.Usage"));
				this.plugin.info(player, langUtils.getString("Lang.Available") + " <" + available.substring(0, available.length() - 1) + ">");
				this.plugin.info(player, Messages.HYPHEN);
				return;
			}

			this.invite(player, myParties, args[0], langUtils);
			return;
		}
		this.printHelp(player, langUtils);
	}

	private void printHelp(ProxiedPlayer player, LanguageUtils langUtils) {
		this.plugin.info(player, Messages.HYPHEN);
		this.plugin.info(player, langUtils.getString("Help.Command"));
		this.plugin.info(player, langUtils.getString("Help.Help"));
		this.plugin.info(player, langUtils.getString("Help.Chat"));
		this.plugin.info(player, langUtils.getString("Help.Invite"));
		this.plugin.info(player, langUtils.getString("Help.Leave"));
		this.plugin.info(player, langUtils.getString("Help.List"));
		this.plugin.info(player, langUtils.getString("Help.Remove"));
		this.plugin.info(player, langUtils.getString("Help.Warp"));
		this.plugin.info(player, langUtils.getString("Help.Accept"));
		this.plugin.info(player, langUtils.getString("Help.Toggle"));
		this.plugin.info(player, langUtils.getString("Help.Lang"));
		this.plugin.info(player, langUtils.getString("Help.Disband"));
		this.plugin.info(player, Messages.HYPHEN);
	}

	private void invite(ProxiedPlayer player, PartyUtils myParties, String args, LanguageUtils langUtils) {
		UUID target = this.plugin.getPlayerManager().getPlayerUniqueId(args);
		if(target == null) {
			this.plugin.info(player, Messages.HYPHEN);
			this.plugin.info(player, langUtils.getString("Cant-Find").replace("%name", args));
			this.plugin.info(player, Messages.HYPHEN);
			return;
		}
		PartyUtils targetParties = this.plugin.getPartyManager().getPlayer(target);

		try {
			myParties.addRequest(target, langUtils);
		} catch (Exception e) {
			this.plugin.info(player, Messages.HYPHEN);
			this.plugin.info(player, e.getMessage());
			this.plugin.info(player, Messages.HYPHEN);
			return;
		}

		this.plugin.info(player, Messages.HYPHEN);
		this.plugin.info(player, langUtils.getString("Invite.Request.Sent.Invited").replace("%displayName", myParties.getDisplayName()).replace("%targetDisplayName", targetParties.getDisplayName()));
		this.plugin.info(player, langUtils.getString("Invite.Request.Sent.60-Seconds"));
		this.plugin.info(player, Messages.HYPHEN);

		for(String partyPlayerUniqueId : myParties.getParties()) {
			ProxiedPlayer partyPlayer = this.plugin.getProxy().getPlayer(UUID.fromString(partyPlayerUniqueId));
			LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(partyPlayer);

			this.plugin.info(partyPlayer, Messages.HYPHEN);
			this.plugin.info(partyPlayer, targetLangUtils.getString("Invite.Request.Sent.Invited").replace("%displayName", myParties.getDisplayName()).replace("%targetDisplayName", targetParties.getDisplayName()));
			this.plugin.info(partyPlayer, Messages.HYPHEN);
		}

		LanguageUtils targetLangUtils = this.plugin.getLanguageManager().getPlayer(target);
		TextComponent prefix = MessageBuilder.get(this.plugin.getPrefix());
		TextComponent invite = MessageBuilder.get(targetLangUtils.getString("Invite.Request.Click-here.Here"), "/party accept " + player.getName(), ChatColor.GOLD, "Click to run\n/party accept " + player.getName(), false);

		this.plugin.info(target, Messages.HYPHEN);
		this.plugin.info(target, targetLangUtils.getString("Invite.Request.Click-here.Receive").replace("%displayName", myParties.getDisplayName()));
		if(targetParties.getPlayer() != null) {
			targetParties.getPlayer().sendMessage(prefix, invite);
		}
		this.plugin.info(target, Messages.HYPHEN);

		ThreadPool.run(new Runnable() {
			@Override
			public void run() {
				try {
					TimeUnit.MINUTES.sleep(1);
				} catch (Exception e) {
				}

				try {
					myParties.removeRequest(target, langUtils);
				} catch (NotInvitedException e) {
					return;
				}

				PartyCommand.this.plugin.info(player, Messages.HYPHEN);
				PartyCommand.this.plugin.info(player, langUtils.getString("Invite.Request.Expired.Your-Self").replace("%targetDisplayName", targetParties.getDisplayName()));
				PartyCommand.this.plugin.info(player, Messages.HYPHEN);

				if(myParties.getParties().size() == 0) {
					PartyCommand.this.plugin.info(player, Messages.HYPHEN);
					PartyCommand.this.plugin.info(player, langUtils.getString("Invite.All-Left"));
					PartyCommand.this.plugin.info(player, Messages.HYPHEN);
				}

				LanguageUtils targetLangUtils = PartyCommand.this.plugin.getLanguageManager().getPlayer(target);
				PartyCommand.this.plugin.info(target, Messages.HYPHEN);
				PartyCommand.this.plugin.info(target, targetLangUtils.getString("Invite.Request.Expired.Target").replace("%displayName", myParties.getDisplayName()));
				PartyCommand.this.plugin.info(target, Messages.HYPHEN);
			}
		});
	}

}
