package net.aquazus.casinobarrierebot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.aquazus.casinobarrierebot.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class DailyCommand extends Command {

    private Bot bot;

    public DailyCommand(Bot bot) {
        this.bot = bot;
        this.name = "daily";
        this.help = "take your daily free chips";
        this.botPermissions = new Permission[]{Permission.VIEW_CHANNEL, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ, Permission.MESSAGE_EMBED_LINKS};
        this.guildOnly = true;
        this.cooldown = 5;
    }

    @Override
    protected void execute(CommandEvent event) {
        long authorId = event.getAuthor().getIdLong();
        if (bot.getDailyCache().containsKey(authorId) && bot.getDailyCache().get(authorId) > System.currentTimeMillis()) {
            event.reply(new EmbedBuilder().setColor(event.getSelfMember().getColor())
                    .setAuthor("Daily Reward", null, Bot.MAIN_ICON_URL)
                    .setDescription(":x: You have already claimed your daily reward today")
                    .setFooter("Requested by " + event.getAuthor().getName(), event.getAuthor().getEffectiveAvatarUrl())
                    .build());
            return;
        }
        bot.getDailyCache().put(authorId, System.currentTimeMillis() + 86400000L);
        long amount = ThreadLocalRandom.current().nextLong(400, 1000);
        bot.addChips(authorId, amount);
        event.reply(new EmbedBuilder().setColor(event.getSelfMember().getColor())
                .setAuthor("Daily Reward", null, Bot.MAIN_ICON_URL)
                .setDescription(":moneybag: The casino gave you **" + NumberFormat.getNumberInstance(Locale.US).format(amount) + "** free chips")
                .setFooter("Requested by " + event.getAuthor().getName(), event.getAuthor().getEffectiveAvatarUrl())
                .build());
    }
}
