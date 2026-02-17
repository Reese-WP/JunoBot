package com.ReeseWP.JunoBot;

import com.google.gson.*;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AI {

    private final String apiKey;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final String systemPrompt;

    private final List<JsonObject> conversation = new ArrayList<>();

    public AI(String systemPrompt) {
        this.apiKey = System.getenv("DEEPSEEK_API_KEY");
        if (apiKey == null)
            throw new IllegalStateException("DEEPSEEK_API_KEY environment variable not set");

        this.httpClient = new OkHttpClient();
        this.gson = new Gson();
        this.systemPrompt = systemPrompt;

        // Add system message at start
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", systemPrompt);

        conversation.add(systemMessage);
    }

    public String send(String input) throws IOException {

        // Add user message
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", input);
        conversation.add(userMessage);

        // Build messages array
        JsonArray messagesArray = new JsonArray();
        for (JsonObject msg : conversation) {
            messagesArray.add(msg);
        }

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("model", "deepseek-chat");
        jsonBody.add("messages", messagesArray);

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url("https://api.deepseek.com/chat/completions")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "no body";
                throw new IOException("Unexpected code " + response.code() + ": " + errorBody);
            }

            String responseBody = response.body().string();
            JsonObject root = gson.fromJson(responseBody, JsonObject.class);

            String content = root
                    .getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content")
                    .getAsString();

            // Add assistant reply to memory
            JsonObject assistantMessage = new JsonObject();
            assistantMessage.addProperty("role", "assistant");
            assistantMessage.addProperty("content", content);
            conversation.add(assistantMessage);

            return content;
        }
    }

    public void clearConversation() {
        conversation.clear();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", systemPrompt);

        conversation.add(systemMessage);
    }
}
