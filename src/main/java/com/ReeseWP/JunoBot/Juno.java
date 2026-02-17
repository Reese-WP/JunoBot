package com.ReeseWP.JunoBot;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.io.IOException;

public class Juno {

    private ShardManager shardManager;
    private static AI junoAI;
    private static String prompt;
    // REAL constructor
    public Juno() throws LoginException {
        String token = System.getenv("JUNO_DISCORD_TOKEN");
        if (token == null)
            throw new IllegalStateException("JUNO_TOKEN environment variable not set");

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.watching("pondering truth"));
        builder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        EventListener eventListener = new EventListener(junoAI);

        builder.addEventListeners(eventListener);

        shardManager = builder.build();
    }

    public static void main(String[] args) {
        prompt = System.getenv("JUNO_AI_PROMPT");
        junoAI = new AI(prompt);
        try {
            new Juno(); // Start the bot
        } catch (LoginException e) {
            System.out.println("Bot failed to log in: " + e.getMessage());
        }

    }

    public ShardManager getShardManager() {
        return shardManager;
    }
}
