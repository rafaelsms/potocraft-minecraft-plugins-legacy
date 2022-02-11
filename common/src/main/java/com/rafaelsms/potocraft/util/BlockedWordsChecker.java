package com.rafaelsms.potocraft.util;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.text.Normalizer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockedWordsChecker {

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
                } else if (c == 'i' || c == 'e' || c == 'y') {
                    stringBuilder.append("|i|1|e|3|y"); // gay gai gei
                } else if (c == 'o' || c == 'u') {
                    stringBuilder.append("|o|0|u");
                } else if (c == 'l') {
                    stringBuilder.append("|u|1");
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
                } else if (c == 'g') {
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

    @SuppressWarnings("SuspiciousRegexArgument")
    public Optional<String> removeBlockedWords(@NotNull String string) {
        int stringLength = string.length();

        // Normalize string
        String normalizedString = Normalizer.normalize(string, Normalizer.Form.NFD);
        normalizedString = normalizedString.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        boolean anyReplacement = false;

        // Search blocked words
        for (Map.Entry<String, Pattern> entry : blockedWordsRegex.entrySet()) {
            // Find match
            Matcher matcher = entry.getValue().matcher(normalizedString);
            while (matcher.find()) {
                // Replace match with symbols
                int start = matcher.start(2);
                int end = matcher.end(2);
                String beforeWord = string.substring(0, Math.max(start, 0));
                String afterWord = string.substring(Math.min(end, stringLength), stringLength); // end is exclusive
                String censuredWord = matcher.group(2).replaceAll(".", "-");

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
                anyReplacement = true;
            }
        }

        return anyReplacement ? Optional.of(string) : Optional.empty();
    }
}
