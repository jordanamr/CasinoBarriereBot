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

public class BlackjackGame {

    public static String ICON_URL = "https://i.imgur.com/Yyygz6x.png";
    private static long EMOJI_HIT = 624278317092044811L;
    private static long EMOJI_STAND = 624278317549223936L;
    private static long EMOJI_DOUBLE = 624278317377519616L;
    private Bot bot;
    private int bet;
    private Member self;
    @Getter
    private User player;
    private TextChannel channel;
    @Getter
    private Message currentMessage;
    private Deck deck;
    private ArrayList<Card> dealerHand = new ArrayList<>();
    private ArrayList<Card> playerHand = new ArrayList<>();
    private GameState state = GameState.FIRST;
    private Timer afkTimer;
    private long afkTimeout = Long.MAX_VALUE;

    public BlackjackGame(Bot bot, int bet, Member self, User player, TextChannel channel) {
        this.bot = bot;
        this.bet = bet;
        this.self = self;
        this.player = player;
        this.channel = channel;
    }

    public void start() {
        deck = new Deck();
        deck.shuffle();
        dealerHand.add(deck.takeCard());
        playerHand.add(deck.takeCard());
        playerHand.add(deck.takeCard());

        if (isBlackjack(playerHand)) {
            state = GameState.BLACKJACK_PLAYER;
        }
        afkTimer = new Timer();
        afkTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        long time = System.currentTimeMillis();
                        if (time >= afkTimeout) {
                            state = GameState.AFK;
                            printState();
                        }
                    }
                }, 1000L, 1000L
        );
        printState();
    }

    private void printState() {
        if (currentMessage != null) {
            currentMessage.delete().queue();
        }
        String description;
        switch (state) {
            case BLACKJACK_PLAYER:
                description = ":money_mouth: You got a BlackJack! You won " + NumberFormat.getNumberInstance(Locale.US).format(Math.round(bet * 2.5))  + " chips";
                break;
            case BLACKJACK_DEALER:
                description = ":x: The dealer got a BlackJack. You lost " + NumberFormat.getNumberInstance(Locale.US).format(bet) + " chips";
                break;
            case BUSTED_PLAYER:
                description = ":fire: You busted. You lost " + NumberFormat.getNumberInstance(Locale.US).format(bet) + " chips";
                break;
            case BUSTED_DEALER:
                description = ":moneybag: The dealer busted! You won " + NumberFormat.getNumberInstance(Locale.US).format(bet * 2) + " chips";
                break;
            case WIN:
                description = ":moneybag: You got a better hand! You won " + NumberFormat.getNumberInstance(Locale.US).format(bet * 2) + " chips";
                break;
            case LOSE:
                description = ":x: The dealer got a better hand. You lost " + NumberFormat.getNumberInstance(Locale.US).format(bet) + " chips";
                break;
            case DRAW:
                description = ":warning: Draw. You lost nothing";
                break;
            case AFK:
                description = ":timer: You lost because you were AFK";
                break;
            default:
                description = ":grey_question: What do you want to do ?";
                break;
        }

        channel.sendMessage(new EmbedBuilder().setColor(self.getColor())
                .setAuthor("BlackJack (Stake: " + NumberFormat.getNumberInstance(Locale.US).format(bet) + ")", null, ICON_URL)
                .setDescription("Dealer's hand\n" + printHand(dealerHand) + "\n\nYour hand\n" + printHand(playerHand) + "\n\n" + description)
                .setFooter(player.getName() + "'s table", player.getEffectiveAvatarUrl())
                .build()).queue(message -> {
            currentMessage = message;
            if (state == GameState.PLAY || state == GameState.FIRST) {
                addActions(message);
            } else {
                terminate();
                return;
            }
            resetAfkTimer();
        });
    }

    private void hit(boolean x2) {
        if (state == GameState.FIRST) {
            state = GameState.PLAY;
        }
        playerHand.add(deck.takeCard());
        int handValue = handValue(playerHand);
        if (isBlackjack(playerHand)) {
            state = GameState.BLACKJACK_PLAYER;
            stand();
            return;
        } else if (handValue > 21) {
            state = GameState.BUSTED_PLAYER;
            printState();
            return;
        } else if (handValue == 21) {
            state = GameState.WIN;
            stand();
            return;
        }
        if (x2) {
            stand();
        } else {
            printState();
        }
    }

    private void stand() {
        if (state == GameState.FIRST) {
            state = GameState.PLAY;
        }
        while (handValue(dealerHand) < 17) {
            dealerHand.add(deck.takeCard());
        }
        int dealerValue = handValue(dealerHand);
        int playerValue = handValue(playerHand);
        if (dealerValue > 21) {
            state = GameState.BUSTED_DEALER;
        } else if (dealerValue == playerValue) {
            state = GameState.DRAW;
        } else if (isBlackjack(dealerHand)) {
            state = GameState.BLACKJACK_DEALER;
        } else if (dealerValue > playerValue) {
            state = GameState.LOSE;
        } else {
            state = GameState.WIN;
        }
        printState();
    }

    private void resetAfkTimer() {
        afkTimeout = System.currentTimeMillis() + 60000L;
    }

    private void addActions(Message message) {
        message.addReaction(Objects.requireNonNull(bot.getJda().getEmoteById(EMOJI_HIT))).queue();
        message.addReaction(Objects.requireNonNull(bot.getJda().getEmoteById(EMOJI_STAND))).queue();
        if (state == GameState.FIRST && bot.getChips(player.getIdLong()) >= bet) {
            message.addReaction(Objects.requireNonNull(bot.getJda().getEmoteById(EMOJI_DOUBLE))).queue();
        }
    }

    private String printHand(ArrayList<Card> hand) {
        StringBuilder builder = new StringBuilder();
        int count = 0;
        boolean as = false;
        for (Card card : hand) {
            if (card.getRank() == 1) as = true;
            builder.append("**").append(card.getEmoji()).append("** ");
            count += card.getRank() >= 10 ? 10 : card.getRank();
        }
        builder.append("*(").append(isBlackjack(hand) ? "21" : ((as && count + 10 <= 21) ? count + "/" + (count + 10) : "" + count)).append(")*");
        return builder.toString();
    }

    private boolean isBlackjack(ArrayList<Card> hand) {
        if (hand.size() != 2) return false;
        int card1 = hand.get(0).getRank();
        int card2 = hand.get(1).getRank();
        return ((card1 >= 10 && card2 == 1) || (card2 >= 10 && card1 == 1));
    }

    private int handValue(ArrayList<Card> hand) {
        int count = 0;
        boolean as = false;
        for (Card card : hand) {
            if (card.getRank() == 1) as = true;
            count += card.getRank() >= 10 ? 10 : card.getRank();
        }
        if (as && count + 10 <= 21) {
            count += 10;
        }
        return count;
    }

    public void terminate() {
        afkTimeout = Long.MAX_VALUE;
        afkTimer.cancel();
        if (state == GameState.BLACKJACK_PLAYER) {
            bot.addChips(player.getIdLong(), Math.round(bet * 2.5));
        } else if (state == GameState.BUSTED_DEALER || state == GameState.WIN) {
            bot.addChips(player.getIdLong(), bet * 2);
        } else if (state == GameState.DRAW) {
            bot.addChips(player.getIdLong(), bet);
        }
        bot.getBlackjackGames().remove(player.getIdLong());
    }

    public void onReact(long emojiId) {
        if (emojiId == EMOJI_HIT) {
            hit(false);
        } else if (emojiId == EMOJI_STAND) {
            stand();
        } else if (emojiId == EMOJI_DOUBLE) {
            if (bot.getChips(player.getIdLong()) < bet || state != GameState.FIRST) {
                return;
            }
            bot.delChips(player.getIdLong(), bet);
            bet *= 2;
            hit(true);
        }
    }

    public enum GameState {
        FIRST, PLAY, BLACKJACK_DEALER, BLACKJACK_PLAYER, BUSTED_PLAYER, BUSTED_DEALER, WIN, LOSE, DRAW, AFK
    }
}
