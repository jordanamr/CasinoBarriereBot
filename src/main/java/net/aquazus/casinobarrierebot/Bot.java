package net.aquazus.casinobarrierebot;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.PingCommand;
import com.jagrosh.jdautilities.examples.command.ShutdownCommand;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.aquazus.casinobarrierebot.commands.*;
import net.aquazus.casinobarrierebot.games.BlackjackGame;
import net.aquazus.casinobarrierebot.logging.UncaughtExceptionLogger;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
public class Bot extends ListenerAdapter {

    public static boolean debugMode = false;
    private static Bot instance = null;

    private static Bot getInstance() {
        if (instance == null) instance = new Bot();
        return instance;
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger());
        Bot.getInstance().init();
    }

    public static String COMMAND_PREFIX = "c!";
    public static String MAIN_ICON_URL = "https://i.imgur.com/Yyygz6x.png";
    @Getter
    private JDA jda;
    @Getter
    private ConcurrentHashMap<Long, Long> chipsCache = new ConcurrentHashMap<>();
    @Getter
    private ConcurrentHashMap<Long, Long> dailyCache = new ConcurrentHashMap<>();
    private ScheduledExecutorService cacheScheduler = Executors.newSingleThreadScheduledExecutor();
    @Getter
    private ConcurrentHashMap<Long, BlackjackGame> blackjackGames = new ConcurrentHashMap<>();

    private void init() {
        log.info("Loading cache...");
        MongoClient mongoClient = MongoClients.create();
        MongoDatabase database = mongoClient.getDatabase("casinobarriere");
        MongoCollection<Document> collection = database.getCollection("users");
        collection.find().forEach((Consumer<? super Document>) document -> {
            Long userId = document.getLong("uid");
            Long chips = document.getLong("chips");
            Long daily = document.getLong("dailyCooldown");
            if (chips != null) {
                chipsCache.put(userId, chips);
            }
            if (daily != null && daily > System.currentTimeMillis()) {
                dailyCache.put(userId, daily);
            }
        });
        mongoClient.close();
        log.info("Cache loaded!");

        String token = "";
        EventWaiter waiter = new EventWaiter();
        CommandClientBuilder client = new CommandClientBuilder();
        client.useDefaultGame();
        client.setOwnerId("621796333669449749");
        client.setEmojis("\u2705", "\u26A0", "\u274C");
        client.setPrefix(COMMAND_PREFIX);

        AboutCommand aboutCommand = new AboutCommand(Color.BLUE, "a casino bot",
                new String[]{"Blackjack", "Slots"}, Permission.ADMINISTRATOR);
        aboutCommand.setReplacementCharacter("\uD83C\uDFB0"); // 🎰
        client.addCommands(aboutCommand,
                new ShutdownCommand(),
                new PingCommand(),
                new ChipsCommand(this),
                new GiveCommand(this),
                new DailyCommand(this),
                new BlackjackCommand(this),
                new SlotsCommand(this));
        try {
            jda = new JDABuilder(AccountType.BOT).setToken(token).setStatus(OnlineStatus.ONLINE)
                    //.setBulkDeleteSplittingEnabled(true)
                    .setActivity(Activity.playing("Loading..."))
                    .addEventListeners(waiter, client.build(), this)
                    .build().awaitReady();

            cacheScheduler.scheduleAtFixedRate(this::saveCache, 5, 5, TimeUnit.MINUTES);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        cacheScheduler.shutdown();
        saveCache();
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        if (!event.getReactionEmote().isEmote()) return;
        if (blackjackGames.containsKey(event.getUser().getIdLong())) {
            BlackjackGame game = blackjackGames.get(event.getUser().getIdLong());
            if (event.getMessageIdLong() == game.getCurrentMessage().getIdLong()) {
                game.onReact(event.getReactionEmote().getIdLong());
            }
        }
    }

    private void saveCache() {
        log.info("Saving cache...");
        MongoClient mongoClient = MongoClients.create();
        MongoDatabase database = mongoClient.getDatabase("casinobarriere");
        MongoCollection<Document> collection = database.getCollection("users");
        HashMap<Long, Document> documents = new HashMap<>();
        for (Map.Entry<Long, Long> chipsData : chipsCache.entrySet()) {
            documents.put(chipsData.getKey(), new Document().append("uid", chipsData.getKey()).append("chips", chipsData.getValue()));
        }
        for (Map.Entry<Long, Long> dailyData : dailyCache.entrySet()) {
            if (!documents.containsKey(dailyData.getKey())) continue;
            documents.get(dailyData.getKey()).append("dailyCooldown", dailyData.getValue());
        }
        for (Document document : documents.values()) {
            if (collection.find(new Document("uid", document.getLong("uid"))).first() != null) {
                collection.updateOne(new Document("uid", document.getLong("uid")), new Document("$set", document));
            } else {
                collection.insertOne(document);
            }
        }
        mongoClient.close();
        log.info("Cache saved!");
    }

    public long getChips(long userId) {
        if (chipsCache.containsKey(userId)) {
            return chipsCache.get(userId);
        }
        setChips(userId, 0L);
        return 0L;
    }

    public void setChips(long userId, long amount) {
        chipsCache.put(userId, amount);
    }

    public void addChips(long userId, long amount) {
        long current = getChips(userId);
        setChips(userId, current + amount);
    }

    public void delChips(long userId, long amount) {
        long current = getChips(userId);
        setChips(userId, current - amount);
    }
}
