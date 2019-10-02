package net.aquazus.casinobarrierebot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.aquazus.casinobarrierebot.Bot;
import net.aquazus.casinobarrierebot.games.BlackjackGame;
import net.aquazus.casinobarrierebot.games.SlotsGame;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

public class SlotsCommand extends Command {

    private Bot bot;

    public SlotsCommand(Bot bot) {
        this.bot = bot;
        this.name = "slots";
        this.aliases = new String[]{"slot", "sl"};
        this.help = "start a game of slot machine";
        this.botPermissions = new Permission[]{Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ,
                Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EXT_EMOJI, Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = true;
        this.cooldown = 10;
    }

    @Override
    protected void execute(CommandEvent event) {
        int bet;
        String command = event.getMessage().getContentRaw();
        try {
            bet = Integer.parseInt(command.split(" ")[1]);
        } catch (Exception ex) {
            bet = -1;
        }

        if (bet < 0) {
            event.reply(new EmbedBuilder().setColor(event.getSelfMember().getColor())
                    .setAuthor("Slots", null, SlotsGame.ICON_URL)
                    .setDescription(":x: Invalid bet amount")
                    .setFooter("Requested by " + event.getAuthor().getName(), event.getAuthor().getEffectiveAvatarUrl())
                    .build());
            return;
        }

        if (bet > bot.getChips(event.getAuthor().getIdLong())) {
            event.reply(new EmbedBuilder().setColor(event.getSelfMember().getColor())
                    .setAuthor("Slots", null, SlotsGame.ICON_URL)
                    .setDescription(":x: You can't afford this bet")
                    .setFooter("Requested by " + event.getAuthor().getName(), event.getAuthor().getEffectiveAvatarUrl())
                    .build());
            return;
        }
        bot.delChips(event.getAuthor().getIdLong(), bet);

        SlotsGame game = new SlotsGame(bot, bet, event.getSelfMember(), event.getAuthor(), event.getTextChannel());
        game.start();
    }
}
