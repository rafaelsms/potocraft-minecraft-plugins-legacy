package com.rafaelsms.potocraft.util;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.text.Normalizer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class BlockedWordsChecker {

    private final Map<String, Pattern> blockedWordsRegex = new HashMap<>();

    private final @NotNull Logger logger;

    public BlockedWordsChecker(@NotNull Logger logger, @NotNull Collection<String> blockedWords) {
        this.logger = logger;

        // [^A-Za-z0-9] differs from \W because of underline ('_')
        for (String blockedWord : blockedWords) {
            StringBuilder stringBuilder = new StringBuilder("(");
            for (char c : blockedWord.toCharArray()) {
                stringBuilder.append("(").append(c);
                if (c == 'a') {
                    stringBuilder.append("|4");
                } else if (c == 'i' || c == 'e' || c == 'y') {
                    stringBuilder.append("|i|1|e|3|y");
                } else if (c == 'o' || c == 'u' || c == 'l') {
                    stringBuilder.append("|o|0|u|l|1");
                } else if (c == 's' || c == 'z') {
                    stringBuilder.append("|s|z|c|\\$|5");
                } else if (c == 'c' || c == 'ç' || c == 'k' || c == 'g') {
                    stringBuilder.append("|c|ç|k|g");
                } else if (c == 'x') {
                    stringBuilder.append("|(c+h+)");
                }
                stringBuilder.append(")+");
            }
            stringBuilder.append(")");
            blockedWordsRegex.put(blockedWord, Pattern.compile(stringBuilder.toString(), Pattern.CASE_INSENSITIVE));
        }
    }

    @SuppressWarnings("SuspiciousRegexArgument")
    public Optional<String> removeBlockedWords(@NotNull String string) {
        int stringLength = string.length();

        // Normalize string
        String normalizedString = Normalizer.normalize(string, Normalizer.Form.NFD);
        normalizedString = normalizedString.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        boolean anyReplacement = false;

        // Search blocked words
        for (Map.Entry<String, Pattern> entry : blockedWordsRegex.entrySet()) {
            // Find every match
            MatchResult matchResult = entry.getValue().matcher(normalizedString).toMatchResult();
            // Replace match with symbols
            for (int i = 0; i < matchResult.groupCount(); i++) {
                int start = matchResult.start(i);
                int end = matchResult.end(i);
                String beforeWord = string.substring(0, Math.max(start - 1, 0));
                String afterWord = string.substring(Math.min(end + 1, stringLength), stringLength);
                String censuredWord = matchResult.group(i).replaceAll(".", "*");

                // Compose the string back
                string = beforeWord + censuredWord + afterWord;
                logger.info(
                        "Found \"{}\" in \"{}\" (start = {}, end = {}) (pattern = \"{}\"), replacing with \"{}\" resulting in {}",
                        entry.getKey(),
                        normalizedString,
                        start,
                        end,
                        entry.getValue().toString(),
                        censuredWord,
                        string);
                anyReplacement = true;
            }
        }

        return anyReplacement ? Optional.of(string) : Optional.empty();
    }
}
