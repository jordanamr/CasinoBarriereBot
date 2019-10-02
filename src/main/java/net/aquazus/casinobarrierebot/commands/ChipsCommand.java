package net.aquazus.casinobarrierebot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.aquazus.casinobarrierebot.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.text.NumberFormat;
import java.util.Locale;

public class ChipsCommand extends Command {

    private Bot bot;

    public ChipsCommand(Bot bot) {
        this.bot = bot;
        this.name = "chips";
        this.aliases = new String[]{"chip"};
        this.help = "check your chip stack";
        this.botPermissions = new Permission[]{Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ, Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = true;
        this.cooldown = 5;
    }

    @Override
    protected void execute(CommandEvent event) {
        long chips = bot.getChips(event.getAuthor().getIdLong());
        event.reply(new EmbedBuilder().setColor(event.getSelfMember().getColor())
                .setAuthor("Chip Stack", null, Bot.MAIN_ICON_URL)
                .setDescription("You have **" + NumberFormat.getNumberInstance(Locale.US).format(chips) + "** chips")
                .setFooter("Requested by " + event.getAuthor().getName(), event.getAuthor().getEffectiveAvatarUrl())
                .build());
    }
}
