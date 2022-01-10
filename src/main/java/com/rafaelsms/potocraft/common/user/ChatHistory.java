package com.rafaelsms.potocraft.common.user;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.Deque;

public abstract class ChatHistory {

    private final Deque<Message> messages = new ArrayDeque<>();

    public ChatResult canSendMessage(@NotNull String messageString) {
        // If player has bypass permission, allow it right away
        if (playerHasBypassPermission()) {
            return ChatResult.ALLOWED;
        }

        // Remove old messages from queue
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime expirationDate = now.minus(getLimiterTimeFrame());
        while (!messages.isEmpty() && messages.peekFirst().dateTime().isBefore(expirationDate)) {
            messages.pollFirst();
        }

        // Check if number of messages exceeds limit, cancel
        int amountOfMessages = messages.size();
        if ((amountOfMessages + 1) > getLimiterMaxMessageAmount()) {
            return ChatResult.TOO_FREQUENT;
        }

        Message lastMessage = messages.peekFirst();
        // Check if we have anything to compare against
        if (lastMessage == null) {
            return ChatResult.ALLOWED;
        }
        // Check if messages are too small to compare
        if (lastMessage.message().length() < getMinLengthToCompare()) {
            return ChatResult.ALLOWED;
        }
        if (messageString.length() < getMinLengthToCompare()) {
            return ChatResult.ALLOWED;
        }

        // Check if last message is similar
        LevenshteinDistance comparator = new LevenshteinDistance(getComparatorThreshold());
        int difference = comparator.apply(messageString, lastMessage.message());
        if (difference >= 0 && difference < getComparatorThreshold()) {
            return ChatResult.SIMILAR_MESSAGES;
        }
        // Otherwise, allow
        return ChatResult.ALLOWED;
    }

    public void sentMessage(@NotNull String messageString) {
        messages.addLast(new Message(messageString, ZonedDateTime.now()));
    }

    protected abstract int getComparatorThreshold();

    protected abstract int getMinLengthToCompare();

    protected abstract @NotNull Duration getLimiterTimeFrame();

    protected abstract int getLimiterMaxMessageAmount();

    protected abstract boolean playerHasBypassPermission();

    private record Message(@NotNull String message, @NotNull ZonedDateTime dateTime) {
    }

    public enum ChatResult {

        SIMILAR_MESSAGES,
        TOO_FREQUENT,
        ALLOWED,
        ;

        public boolean isAllowed() {
            return this == ALLOWED;
        }
    }

}
