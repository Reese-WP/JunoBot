package com.ReeseWP.JunoBot;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;

public class EventListener extends ListenerAdapter {
    private AI junoAI;

    public EventListener(AI ai){
        junoAI = ai;
    }
    public void onMessageReceived(MessageReceivedEvent event) {
        System.out.println(event.getAuthor());
        System.out.println(event.getJDA().getSelfUser());
        if (event.getAuthor().equals(event.getJDA().getSelfUser())) return;

        Message message = event.getMessage();
        MessageChannel channel = event.getChannel();
        String content = message.getContentRaw();

        try {
            String jsonResponse = junoAI.send(content);
            channel.sendMessage(jsonResponse).queue();
            System.out.println(jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
