package net.aquazus.casinobarrierebot.games;

import lombok.Getter;
import net.aquazus.casinobarrierebot.Bot;
import net.aquazus.casinobarrierebot.base.Card;
import net.aquazus.casinobarrierebot.base.Deck;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SlotsGame {

    public static String ICON_URL = "https://i.imgur.com/zwnfjM1.png";
    private static long SPIN_DELAY = 500L;
    private Bot bot;
    private int bet;
    private Member self;
    @Getter
    private User player;
    private TextChannel channel;
    private EmbedBuilder builder;
    private Message currentMessage;
    private int spinCount = 0;

    public SlotsGame(Bot bot, int bet, Member self, User player, TextChannel channel) {
        this.bot = bot;
        this.bet = bet;
        this.self = self;
        this.player = player;
        this.channel = channel;
    }

    public void start() {
        builder = new EmbedBuilder().setColor(self.getColor()).setAuthor("Slots (Stake: " + NumberFormat.getNumberInstance(Locale.US).format(bet) + ")", null, ICON_URL)
                .setFooter(player.getName() + "'s machine", player.getEffectiveAvatarUrl());

        channel.sendMessage(builder.setDescription(generateSpin()).build()).queue(message -> {
            currentMessage = message;
            spin();
        });
    }

    private void spin() {
        try {
            Thread.sleep(SPIN_DELAY);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        spinCount++;
        currentMessage.editMessage(builder.setDescription(generateSpin()).build()).queue(message -> {
            currentMessage = message;
            if (spinCount < 3) {
                spin();
            } else {
                finalSpin();
            }
        }, message -> spin());
    }

    private void finalSpin() {
        try {
            Thread.sleep(SPIN_DELAY);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        Fruit fruit1 = Fruit.randomFruit();
        Fruit fruit2 = Fruit.randomFruit();
        Fruit fruit3 = Fruit.randomFruit();

        String[] spinTable = generateSpin().split("\n");
        spinTable[3] = fruit1.emoji + " : " + fruit2.emoji + " : " + fruit3.emoji + " **<-**";
        StringBuilder spinBuilder = new StringBuilder();
        for (int i = 0; i < spinTable.length; i++) {
            spinBuilder.append(spinTable[i]);
            spinBuilder.append("\n");
        }
        String spin = spinBuilder.toString();

        int potentialWinnings = (int) Math.round(bet * fruit1.multiplier);

        boolean win = false;
        boolean refund = false;
        if (fruit1 == fruit2 && fruit2 == fruit3) {
            win = true;
            spin += "**| : : : WIN : : : |**\n\nYou inserted **" + bet + "** chip" + (bet > 1 ? "s" : "") + " in the machine and won **" + potentialWinnings + "** chips!";
        } else if (fruit1 == fruit2 || fruit2 == fruit3 || fruit1 == fruit3) {
            refund = true;
            spin += "**| : : REFUND : : |**\n\nYou inserted **" + bet + "** chip" + (bet > 1 ? "s" : "") + " in the machine and got your **" + bet + "** chips back!";
        } else {
            spin += "**| : : : LOST : : : |**\n\nYou inserted **" + bet + "** chip" + (bet > 1 ? "s" : "") + " in the machine and lost everything.";
        }

        currentMessage.editMessage(builder.setDescription(spin).build()).queue();
        if (win) {
            bot.addChips(player.getIdLong(), potentialWinnings);
        } else if (refund) {
            bot.addChips(player.getIdLong(), bet);
        }
    }

    private String generateSpin() {
        return "**[  :slot_machine: l SLOTS ]**\n**------------------**\n" + Fruit.randomFruit().emoji + " : " + Fruit.randomFruit().emoji + " : " + Fruit.randomFruit().emoji + "\n" + Fruit.randomFruit().emoji + " : " + Fruit.randomFruit().emoji + " : " + Fruit.randomFruit().emoji + " **<-**\n" + Fruit.randomFruit().emoji + " : " + Fruit.randomFruit().emoji + " : " + Fruit.randomFruit().emoji +"\n**------------------**";
    }

    public enum Fruit {
        BANANA(":banana:", 2d),
        CHERRIES(":cherries:", 3d),
        GRAPES(":grapes:", 4d),
        WATERMELON(":watermelon:", 5d),
        PEAR(":pear:", 6d),
        TANGERINE(":tangerine:", 7d),
        MELON(":melon:", 8d),
        SEVEN(":seven:", 10d);

        public String emoji;
        public double multiplier;

        Fruit(String emoji, double multiplier) {
            this.emoji = emoji;
            this.multiplier = multiplier;
        }

        public static Fruit randomFruit()  {
            return Fruit.values()[(int) (ThreadLocalRandom.current().nextDouble() * Fruit.values().length)];
        }
    }
}
