package com.rafaelsms.potocraft.util;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.text.Normalizer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockedWordsChecker {

    private final Map<String, Pattern> blockedWordsRegex = new HashMap<>();

    private final @NotNull Logger logger;

    public BlockedWordsChecker(@NotNull Logger logger, @NotNull Collection<String> blockedWords) {
        this.logger = logger;

        // [^A-Za-z0-9] differs from \W because of underline ('_')
        for (String blockedWord : blockedWords) {
            StringBuilder stringBuilder = new StringBuilder("^((.*[^A-Za-z0-9]+)|([^A-Za-z0-9]*))");
            for (char c : blockedWord.toCharArray()) {
                // Check leet, x and *
                stringBuilder.append("(").append(c);
                if (c == 'a') {
                    stringBuilder.append("|a|4");
                } else if (c == 'i' || c == 'e') {
                    stringBuilder.append("|i|1|e|3");
                } else if (c == 'o' || c == 'u') {
                    stringBuilder.append("|o|0|u");
                } else if (c == 's' || c == 'z') {
                    stringBuilder.append("|s|z|c|\\$|5");
                } else if (c == 'c' || c == 'ç' || c == 'k' || c == 'g') {
                    stringBuilder.append("|c|ç|k|g");
                }
                stringBuilder.append(")+");
                stringBuilder.append("([^A-Za-z0-9]*)");
            }
            stringBuilder.append("(([^A-Za-z0-9]*)|([^A-Za-z0-9]+.*)?)$");
            blockedWordsRegex.put(blockedWord, Pattern.compile(stringBuilder.toString(), Pattern.CASE_INSENSITIVE));
        }
    }

    public boolean containsBlockedWord(@NotNull String string) {
        string = Normalizer.normalize(string, Normalizer.Form.NFD);
        string = string.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        for (Map.Entry<String, Pattern> entry : blockedWordsRegex.entrySet()) {
            Matcher matcher = entry.getValue().matcher(string);
            if (matcher.matches()) {
                logger.info("Found \"{}\" in \"{}\" (pattern = \"{}\")",
                            entry.getKey(),
                            string,
                            entry.getValue().toString());
                return true;
            }
        }
        return false;
    }
}
