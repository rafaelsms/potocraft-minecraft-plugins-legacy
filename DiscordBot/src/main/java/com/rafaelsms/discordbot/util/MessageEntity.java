package com.rafaelsms.discordbot.util;

import net.dv8tion.jda.api.entities.Message;

import java.time.ZonedDateTime;

public record MessageEntity(Message message, ZonedDateTime dateTime) {
}
