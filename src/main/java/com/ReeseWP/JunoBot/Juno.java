package com.ReeseWP.JunoBot;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
public class Juno  {

    private final ShardManager shardManager;

    public Juno throws LoginException {
        String token = System.getenv("JUNO_TOKEN");
        if (token == null)
            throw new IllegalAccessException("token not set");
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.watching("pondering truth"));
        builder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        builder.addEventListeners(new Eve());

    }
    public static void main(String[] args) {
        try {
            Juno bot = new Juno();
        } cach (LoginException e) {
            System.out.println("problem making bot object");
        }
    }
    public ShardManager getShardManager() {
        return shardManager;
    }
}