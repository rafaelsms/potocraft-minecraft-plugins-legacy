package com.rafaelsms.discordbot;

public class Main {

    public static void main(String[] args) {
        try {
            final DiscordBot discordBot = new DiscordBot();
        } catch (Exception exception) {
            exception.printStackTrace();
            System.exit(-1);
        }
    }

}
