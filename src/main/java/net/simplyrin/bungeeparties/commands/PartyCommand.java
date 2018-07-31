package net.simplyrin.bungeeparties.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.simplyrin.bungeefriends.utils.MessageBuilder;
import net.simplyrin.bungeeparties.Main;
import net.simplyrin.bungeeparties.exceptions.AlreadyJoinedException;
import net.simplyrin.bungeeparties.exceptions.FailedAddingException;
import net.simplyrin.bungeeparties.exceptions.NotInvitedException;
import net.simplyrin.bungeeparties.exceptions.NotJoinedException;
import net.simplyrin.bungeeparties.messages.Messages;
import net.simplyrin.bungeeparties.messages.Permissions;
import net.simplyrin.bungeeparties.utils.PartyManager.PartyUtils;
import net.simplyrin.threadpool.ThreadPool;

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

		if(!player.hasPermission(Permissions.MAIN)) {
			this.plugin.info(player, Messages.NO_PERMISSION);
			return;
		}

		if(args.length > 0) {
			if(args[0].equalsIgnoreCase("invite")) {
				if(args.length > 1) {
					this.invite(player, myParties, args[1]);
					return;
				}
				this.plugin.info(player, Messages.HYPHEN);
				this.plugin.info(player, "&cInvalid usage! '/party invite <player>'");
				this.plugin.info(player, Messages.HYPHEN);
				return;
			}

			if(args[0].equalsIgnoreCase("leave")) {
				if(this.plugin.getConfigManager().getConfig().getString("Player." + myParties.getPlayer().getUniqueId() + ".Currently-Joined-Party").equals("NONE")) {
					this.plugin.info(player, Messages.HYPHEN);
					this.plugin.info(player, "&cYou must be in a party to use this command!");
					this.plugin.info(player, Messages.HYPHEN);
					return;
				}

				PartyUtils partyLeader = myParties.leaveCurrentParty();

				this.plugin.info(player, Messages.HYPHEN);
				this.plugin.info(player, "&aYou left the party");
				this.plugin.info(player, Messages.HYPHEN);

				if(myParties.getParties().size() > 0) {
					this.plugin.info(partyLeader.getPlayer(), Messages.HYPHEN);
					this.plugin.info(partyLeader.getPlayer(), myParties.getDisplayName() + "&e left the party.");
					this.plugin.info(partyLeader.getPlayer(), Messages.HYPHEN);

					for(String partyPlayerUniqueId : myParties.getParties()) {
						ProxiedPlayer partyPlayer = this.plugin.getProxy().getPlayer(partyPlayerUniqueId);
						this.plugin.info(partyPlayer, Messages.HYPHEN);
						this.plugin.info(partyPlayer, myParties.getDisplayName() + "&e left the party.");
						this.plugin.info(partyPlayer, Messages.HYPHEN);
					}
				}
				return;
			}

			if(args[0].equalsIgnoreCase("list")) {
				if(this.plugin.getConfigManager().getConfig().getString("Player." + myParties.getPlayer().getUniqueId() + ".Currently-Joined-Party").equals("NONE")) {
					this.plugin.info(player, Messages.HYPHEN);
					this.plugin.info(player, "&cYou must be in a party to use this command!");
					this.plugin.info(player, Messages.HYPHEN);
					return;
				}

				PartyUtils partyLeader;
				try {
					partyLeader = myParties.getPartyLeader();
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
				this.plugin.info(player, "&aParty members (" + (parties.size() + 1) + "): " + raw);
				this.plugin.info(player, Messages.HYPHEN);
				return;
			}

			if(args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("kick")) {
				if(args.length > 1) {
					if(this.plugin.getConfigManager().getConfig().getString("Player." + myParties.getPlayer().getUniqueId() + ".Currently-Joined-Party").equals("NONE")) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, "&cYou must be in a party to use this command!");
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}

					UUID target = this.plugin.getPlayerManager().getPlayerUniqueId(args[1]);
					if(target == null) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, "&cCan't find a player by the name of '" + args[1] + "'");
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}
					PartyUtils targetParties = this.plugin.getPartyManager().getPlayer(target);

					try {
						myParties.remove(target);
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
							this.plugin.info(partyPlayer, targetParties.getDisplayName() + " &ahas been removed from your party!");
							this.plugin.info(partyPlayer, Messages.HYPHEN);
						}
					}

					this.plugin.info(target, Messages.HYPHEN);
					this.plugin.info(target, "&eYou have been kicked from the party by " + myParties.getDisplayName() + "&e!");
					this.plugin.info(target, Messages.HYPHEN);
					return;
				}
				this.plugin.info(player, Messages.HYPHEN);
				this.plugin.info(player, "&cInvalid usage! '/party remove <player>'");
				this.plugin.info(player, Messages.HYPHEN);
				return;
			}

			if(args[0].equalsIgnoreCase("warp")) {
				if(this.plugin.getConfigManager().getConfig().getString("Player." + myParties.getPlayer().getUniqueId() + ".Currently-Joined-Party").equals("NONE")) {
					this.plugin.info(player, Messages.HYPHEN);
					this.plugin.info(player, "&cYou must be in a party to use this command!");
					this.plugin.info(player, Messages.HYPHEN);
					return;
				}

				this.plugin.info(player, Messages.HYPHEN);
				this.plugin.info(player, "&eThe party leader " + myParties.getDisplayName() + " &esummoned you to their server.");
				this.plugin.info(player, Messages.HYPHEN);

				for(String partyPlayerUniqueId : myParties.getParties()) {
					ProxiedPlayer targetPlayer = this.plugin.getProxy().getPlayer(UUID.fromString(partyPlayerUniqueId));
					if(targetPlayer != null) {
						targetPlayer.connect(myParties.getPlayer().getServer().getInfo());
					}

					this.plugin.info(targetPlayer, Messages.HYPHEN);
					this.plugin.info(targetPlayer, "&eThe party leader " + myParties.getDisplayName() + " &esummoned you to their server.");
					this.plugin.info(targetPlayer, Messages.HYPHEN);
				}
				return;
			}

			if(args[0].equalsIgnoreCase("accept")) {
				if(args.length > 1) {
					UUID target = this.plugin.getPlayerManager().getPlayerUniqueId(args[1]);
					if(target == null) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, "&cCan't find a player by the name of '" + args[1] + "'");
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}
					PartyUtils targetParties = this.plugin.getPartyManager().getPlayer(target);

					try {
						targetParties.removeRequest(player);
					} catch (NotInvitedException e) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, e.getMessage());
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}

					this.plugin.info(player, Messages.HYPHEN);
					this.plugin.info(player, "&aYou joined " + targetParties.getDisplayName() + "&a's party!");
					this.plugin.info(player, Messages.HYPHEN);

					this.plugin.info(targetParties.getPlayer(), Messages.HYPHEN);
					this.plugin.info(targetParties.getPlayer(), myParties.getDisplayName() + "&a joined the party!");
					this.plugin.info(targetParties.getPlayer(), Messages.HYPHEN);

					for(String partyPlayerUniqueId : targetParties.getParties()) {
						PartyUtils targetPlayer = this.plugin.getPartyManager().getPlayer(partyPlayerUniqueId);

						this.plugin.info(targetPlayer.getPlayer(), Messages.HYPHEN);
						this.plugin.info(targetPlayer.getPlayer(), "&a" + myParties.getDisplayName() + " &ajoined the party!");
						this.plugin.info(targetPlayer.getPlayer(), Messages.HYPHEN);
					}

					try {
						targetParties.add(player);
					} catch (AlreadyJoinedException e) {
						e.printStackTrace();
					} catch (FailedAddingException e) {
						e.printStackTrace();
					}
					return;
				}
				this.plugin.info(player, Messages.HYPHEN);
				this.plugin.info(player, "&cInvalid usage! '/party accept <player>'");
				this.plugin.info(player, Messages.HYPHEN);
				return;
			}

			if(args[0].equalsIgnoreCase("disband")) {
				try {
					if(!myParties.isPartyOwner()) {
						this.plugin.info(player, Messages.HYPHEN);
						this.plugin.info(player, "&cYou must be the Party Leader to use that command!");
						this.plugin.info(player, Messages.HYPHEN);
						return;
					}
				} catch (NotJoinedException e) {
					e.printStackTrace();
				}

				this.plugin.info(player, Messages.HYPHEN);
				this.plugin.info(player, myParties.getDisplayName() + "&e has disbanded the party!");
				this.plugin.info(player, Messages.HYPHEN);

				for(String partyPlayerUniqueId : myParties.getParties()) {
					PartyUtils targetPlayer = this.plugin.getPartyManager().getPlayer(partyPlayerUniqueId);

					this.plugin.info(targetPlayer.getPlayer(), Messages.HYPHEN);
					this.plugin.info(targetPlayer.getPlayer(), myParties.getDisplayName() + "&e has disbanded the party!");
					this.plugin.info(targetPlayer.getPlayer(), Messages.HYPHEN);

					this.plugin.getConfigManager().getConfig().set("Player." + targetPlayer.getUniqueId() + ".Currently-Joined-Party", "NONE");
					this.plugin.getConfigManager().getConfig().set("Player." + targetPlayer.getUniqueId() + ".Party-List", new ArrayList<>());
					this.plugin.getConfigManager().getConfig().set("Player." + targetPlayer.getUniqueId() + ".Requests", new ArrayList<>());
				}

				this.plugin.getConfigManager().getConfig().set("Player." + myParties.getUniqueId() + ".Currently-Joined-Party", "NONE");
				this.plugin.getConfigManager().getConfig().set("Player." + myParties.getUniqueId() + ".Party-List", new ArrayList<>());
				this.plugin.getConfigManager().getConfig().set("Player." + myParties.getUniqueId() + ".Requests", new ArrayList<>());
				return;
			}

			if(args[0].equalsIgnoreCase("help")) {
				this.printHelp(player);
				return;
			}

			this.invite(player, myParties, args[0]);
			return;
		}
		this.printHelp(player);
	}

	private void printHelp(ProxiedPlayer player) {
		this.plugin.info(player, Messages.HYPHEN);
		this.plugin.info(player, "&aParty Commands:");
		this.plugin.info(player, "&e/party help &7- &bPrints this help message");
		this.plugin.info(player, "&e/party invite &7- &bInvites the player to your party");
		this.plugin.info(player, "&e/party leave &7- &bLeaves the current party");
		this.plugin.info(player, "&e/party list &7- &bLists the members of your party");
		this.plugin.info(player, "&e/party remove &7- &bRemove the player from the party");
		this.plugin.info(player, "&e/party warp &7- &bTeleport the members of your party to your server");
		this.plugin.info(player, "&e/party accept &7- &bAccept a party invite from the player");
		this.plugin.info(player, "&e/party disband &7- &bDisbands the party");
		this.plugin.info(player, Messages.HYPHEN);
	}

	private void invite(ProxiedPlayer player, PartyUtils myParties, String args) {
		UUID target = this.plugin.getPlayerManager().getPlayerUniqueId(args);
		if(target == null) {
			this.plugin.info(player, Messages.HYPHEN);
			this.plugin.info(player, "&cCan't find a player by the name of '" + args + "'");
			this.plugin.info(player, Messages.HYPHEN);
			return;
		}
		PartyUtils targetParties = this.plugin.getPartyManager().getPlayer(target);

		try {
			myParties.addRequest(target);
		} catch (Exception e) {
			this.plugin.info(player, Messages.HYPHEN);
			this.plugin.info(player, e.getMessage());
			this.plugin.info(player, Messages.HYPHEN);
			return;
		}

		this.plugin.info(player, Messages.HYPHEN);
		this.plugin.info(player, myParties.getDisplayName() + " &einvited " + targetParties.getDisplayName() + "&e to the party!");
		this.plugin.info(player, "&eThey have 60 seconds to accept it!");
		this.plugin.info(player, Messages.HYPHEN);

		for(String partyPlayerUniqueId : myParties.getParties()) {
			ProxiedPlayer partyPlayer = this.plugin.getProxy().getPlayer(UUID.fromString(partyPlayerUniqueId));
			this.plugin.info(partyPlayer, Messages.HYPHEN);
			this.plugin.info(partyPlayer, myParties.getDisplayName() + " &einvited " + targetParties.getDisplayName() + "&e to the party!");
			this.plugin.info(partyPlayer, Messages.HYPHEN);
		}

		TextComponent prefix = MessageBuilder.get(this.plugin.getPrefix());
		TextComponent invite = MessageBuilder.get("Click here ", "/party accept SimplyRin", ChatColor.GOLD, "Click to run\n/party accept SimplyRin", false);
		TextComponent message = MessageBuilder.get("to join! You have 60 seconds to accept.", "/party accept SimplyRin", ChatColor.YELLOW, "Click to run\n/party accept SimplyRin", false);

		this.plugin.info(target, Messages.HYPHEN);
		this.plugin.info(target, myParties.getDisplayName() + "&e has invited you to join their party!");
		if(targetParties.getPlayer() != null) {
			targetParties.getPlayer().sendMessage(prefix, invite, message);
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
					myParties.removeRequest(target);
				} catch (NotInvitedException e) {
					return;
				}

				PartyCommand.this.plugin.info(player, Messages.HYPHEN);
				PartyCommand.this.plugin.info(player, "&eThe party invite to " + targetParties.getDisplayName() + "&e has expired.");
				PartyCommand.this.plugin.info(player, Messages.HYPHEN);

				if(myParties.getParties().size() == 0) {
					PartyCommand.this.plugin.info(player, Messages.HYPHEN);
					PartyCommand.this.plugin.info(player, "&eThe party was disbanded because all invites have expired and all members have left.");
					PartyCommand.this.plugin.info(player, Messages.HYPHEN);
				}

				PartyCommand.this.plugin.info(target, Messages.HYPHEN);
				PartyCommand.this.plugin.info(target, "&eThe party invite from " + myParties.getDisplayName() + "&e has expired.");
				PartyCommand.this.plugin.info(target, Messages.HYPHEN);
			}
		});
	}

}
