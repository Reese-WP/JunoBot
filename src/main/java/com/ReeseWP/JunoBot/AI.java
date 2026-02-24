package com.ReeseWP.JunoBot;

import com.google.gson.*;
import okhttp3.*;

import java.io.IOException;
import java.io.*;
import java.nio.file.*;

public class AI {

    private String apiKey;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final String systemPrompt;

    private final JsonArray conversation = new JsonArray();
    private static final String MemoryLocation = "Memory.json";

    public AI(String systemPrompt) {
        //load api key
        this.apiKey = System.getenv("JUNO_AI_TOKEN");
        if (apiKey == null)
            throw new IllegalStateException("JUNO_AI_TOKEN environment variable not set, given token: " + apiKey);

        //inititate objects / variables
        this.httpClient = new OkHttpClient();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.systemPrompt = systemPrompt;

        // Add system message at start
        try {
            loadConversation();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (conversation.size() == 0) {
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", systemPrompt);
            conversation.add(systemMessage);

        }

    }

    public String send(String input) throws IOException {

        // Add user message
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", input);
        conversation.add(userMessage);

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("model", "deepseek-chat");
        jsonBody.add("messages", conversation);

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
            saveConversation();
            System.out.println(Paths.get(MemoryLocation).toAbsolutePath());

            return content;
        }
    }

    private void saveConversation() throws IOException {
        try (Writer writer = Files.newBufferedWriter(Paths.get(MemoryLocation))) {
            gson.toJson(conversation, writer);
        }
    }

    private void loadConversation() throws IOException {
        Path path = Paths.get(MemoryLocation);

        if (Files.exists(path)) {

            if (Files.size(path) == 0) {
                return; // empty file, nothing to load
            }

            try (Reader reader = Files.newBufferedReader(path)) {

                JsonArray loaded = gson.fromJson(reader, JsonArray.class);

                if (loaded == null) {
                    return; // invalid or empty JSON
                }

                int i = 0;
                while(!conversation.isEmpty()) {conversation.remove(i); i++;}

                for (JsonElement element : loaded) {
                    conversation.add(element);
                }
            }
        }
    }


}
