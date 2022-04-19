package com.rafaelsms.potocraft.player;

import com.rafaelsms.potocraft.util.TextUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockedWordsChecker {

    private static final Random RANDOM = new Random();
    private static final String BAD_WORD_REPLACER = "$#@*%&";

    private final Map<String, Pattern> blockedWordsRegex = new HashMap<>();

    private final @NotNull Logger logger;

    public BlockedWordsChecker(@NotNull Logger logger, @NotNull Collection<String> blockedWords) {
        this.logger = logger;

        // [^A-Za-z0-9] differs from \W because of underline ('_')
        for (String blockedWord : blockedWords) {
            StringBuilder stringBuilder = new StringBuilder("(^|[^A-Za-z0-9]+)(");
            for (char c : blockedWord.toCharArray()) {
                stringBuilder.append("(").append(c);
                if (c == 'a') {
                    stringBuilder.append("|4");
                } else if (c == 'i' || c == 'y') {
                    stringBuilder.append("|i|1|y"); // gay gai // gei is ignored (pega -> pica with it)
                } else if (c == 'e') {
                    stringBuilder.append("|i|1|3"); // ez iz 3z
                } else if (c == 'o') {
                    stringBuilder.append("|o|0");
                } else if (c == 'z') {
                    stringBuilder.append("|s|z"); // gozar gosar
                } else if (c == 's') {
                    stringBuilder.append("|s|z|\\$|5"); // gostosa gostoza gosto$a
                } else if (c == 'ç') {
                    stringBuilder.append("|c|(s+)"); // desgraçada desgracada desgrassada
                } else if (c == 'c') {
                    stringBuilder.append("|ç|k|g"); // babaca babaka carai garai karai
                } else if (c == 'k') {
                    stringBuilder.append("|c|k|g");
                } else if (c == 'x') {
                    stringBuilder.append("|(c+h+)");
                }
                stringBuilder.append(")+");
            }
            stringBuilder.append(")([^A-Za-z0-9]+|$)");
            blockedWordsRegex.put(blockedWord, Pattern.compile(stringBuilder.toString(), Pattern.CASE_INSENSITIVE));
        }
    }

    private Pair<String, Boolean> removeBlockedWord(@NotNull String string, boolean replaced) {
        int stringLength = string.length();

        // Normalize string
        String normalizedString = TextUtil.normalizeString(string);

        // Search blocked words
        for (Map.Entry<String, Pattern> entry : blockedWordsRegex.entrySet()) {
            // Find match
            Matcher matcher = entry.getValue().matcher(normalizedString);
            if (matcher.find()) {
                // Replace match with symbols
                int start = matcher.start(2);
                int end = matcher.end(2);
                String beforeWord = string.substring(0, Math.max(start, 0));
                String afterWord = string.substring(Math.min(end, stringLength), stringLength); // end is exclusive
                String censuredWord = hideBadWord(matcher.group(2));
                // TODO add option to show uncensored to player?

                // Compose the string back
                string = beforeWord + censuredWord + afterWord;
                logger.info(
                        "Found \"{}\" (pattern = \"{}\") in \"{}\" (start = {}, end = {}, group = \"{}\"), replacing with \"{}\" resulting in \"{}\"",
                        entry.getKey(),
                        entry.getValue().toString(),
                        normalizedString,
                        start,
                        end,
                        matcher.group(2),
                        censuredWord,
                        string);
                return removeBlockedWord(string, true);
            }
        }
        return new Pair<>(string, replaced);
    }

    private String hideBadWord(@NotNull String word) {
        // Just to be sure that we don't throw
        if (word.length() < 1) {
            return "";
        }

        // Replace each character with a special character except for the first one
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(word.charAt(0));
        // For aesthetics only, offset bad word characters so each blocked word look different
        ArrayList<Character> chars = new ArrayList<>(BAD_WORD_REPLACER.length());
        for (char c : BAD_WORD_REPLACER.toCharArray()) {
            chars.add(c);
        }
        Collections.shuffle(chars, RANDOM);
        for (int i = 1; i < word.length(); i++) {
            char c = chars.get(i % chars.size());
            if (c == '*') {
                stringBuilder.append("\\*");
            } else {
                stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }

    public Optional<String> removeBlockedWords(@NotNull String string) {
        Pair<String, Boolean> pair = removeBlockedWord(string, false);
        if (pair.second) {
            return Optional.of(pair.first);
        }
        return Optional.empty();
    }

    private record Pair<R, S>(R first, S second) {
    }
}
