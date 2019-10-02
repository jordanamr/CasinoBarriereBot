package net.aquazus.casinobarrierebot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.aquazus.casinobarrierebot.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public class GiveCommand extends Command {

    private Bot bot;

    public GiveCommand(Bot bot) {
        this.bot = bot;
        this.name = "give";
        this.help = "(admin) give chips";
        this.botPermissions = new Permission[]{Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ, Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = true;
        this.ownerCommand = true;
        this.cooldown = 5;
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getMessage().getContentRaw().substring(Bot.COMMAND_PREFIX.length() + this.name.length() + 1).split(" ");
        Member target;
        if (args[0].contains("#")) {
            target = Objects.requireNonNull(event.getGuild().getMemberByTag(args[0]));
        } else {
            target = Objects.requireNonNull(event.getGuild().getMemberById(args[0]));
        }
        long amount = Long.parseLong(args[1]);
        event.reply(new EmbedBuilder().setColor(event.getSelfMember().getColor())
                .setAuthor("Chips Management", null, Bot.MAIN_ICON_URL)
                .setDescription("You added **" + NumberFormat.getNumberInstance(Locale.US).format(amount) + "** chips into " + target.getEffectiveName() + "'s stack")
                .setFooter("Requested by " + event.getAuthor().getName(), event.getAuthor().getEffectiveAvatarUrl())
                .build());
        bot.addChips(target.getIdLong(), amount);
    }
}
