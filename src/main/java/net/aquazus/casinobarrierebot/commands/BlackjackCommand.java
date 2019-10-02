package net.aquazus.casinobarrierebot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.aquazus.casinobarrierebot.Bot;
import net.aquazus.casinobarrierebot.games.BlackjackGame;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

public class BlackjackCommand extends Command {

    private Bot bot;

    public BlackjackCommand(Bot bot) {
        this.bot = bot;
        this.name = "blackjack";
        this.aliases = new String[]{"bj"};
        this.help = "start a game of blackjack";
        this.botPermissions = new Permission[]{Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ,
                Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EXT_EMOJI, Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = true;
        this.cooldown = 5;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (bot.getBlackjackGames().containsKey(event.getAuthor().getIdLong())) {
            event.reply(new EmbedBuilder().setColor(event.getSelfMember().getColor())
                            .setAuthor("BlackJack", null, BlackjackGame.ICON_URL)
                            .setDescription("You are already playing a game of Blackjack")
                            .setFooter("Requested by " + event.getAuthor().getName(), event.getAuthor().getEffectiveAvatarUrl())
                            .build());
            return;
        }
        int bet;
        String command = event.getMessage().getContentRaw();
        try {
            bet = Integer.parseInt(command.split(" ")[1]);
        } catch (Exception ex) {
            bet = -1;
        }

        if (bet < 0) {
            event.reply(new EmbedBuilder().setColor(event.getSelfMember().getColor())
                    .setAuthor("BlackJack", null, BlackjackGame.ICON_URL)
                    .setDescription(":x: Invalid bet amount")
                    .setFooter("Requested by " + event.getAuthor().getName(), event.getAuthor().getEffectiveAvatarUrl())
                    .build());
            return;
        }

        if (bet > bot.getChips(event.getAuthor().getIdLong())) {
            event.reply(new EmbedBuilder().setColor(event.getSelfMember().getColor())
                    .setAuthor("BlackJack", null, BlackjackGame.ICON_URL)
                    .setDescription(":x: You can't afford this bet")
                    .setFooter("Requested by " + event.getAuthor().getName(), event.getAuthor().getEffectiveAvatarUrl())
                    .build());
            return;
        }
        bot.delChips(event.getAuthor().getIdLong(), bet);

        BlackjackGame game = new BlackjackGame(bot, bet, event.getSelfMember(), event.getAuthor(), event.getTextChannel());
        bot.getBlackjackGames().put(event.getAuthor().getIdLong(), game);
        game.start();
    }
}
