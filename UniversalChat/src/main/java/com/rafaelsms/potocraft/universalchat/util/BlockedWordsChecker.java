package com.rafaelsms.potocraft.universalchat.util;

import org.jetbrains.annotations.NotNull;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class BlockedWordsChecker {

    private final List<Pattern> blockedWordsRegex = new ArrayList<>();

    public BlockedWordsChecker(@NotNull Collection<String> blockedWords) {
        for (String blockedWord : blockedWords) {
            StringBuilder stringBuilder = new StringBuilder();
            for (char c : blockedWord.toCharArray()) {
                // Check leet and *
                if (c == 'a') {
                    stringBuilder.append("(a|4|\\*|x)");
                } else if (c == 'i' || c == 'e') {
                    stringBuilder.append("(i|1|e|3|\\*|x)");
                } else if (c == 'o' || c == 'u') {
                    stringBuilder.append("(o|0|u|\\*|x)");
                } else if (c == 's' || c == 'z') {
                    stringBuilder.append("(s|z|\\*|x)");
                } else {
                    stringBuilder.append("(").append(c).append("|\\*|x)");
                }
                stringBuilder.append("\\s*");
            }
            blockedWordsRegex.add(Pattern.compile(stringBuilder.toString(), Pattern.CASE_INSENSITIVE));
        }
    }

    public boolean containsBlockedWord(@NotNull String string) {
        string = Normalizer.normalize(string, Normalizer.Form.NFD);
        string = string.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        for (Pattern pattern : blockedWordsRegex) {
            if (pattern.matcher(string).find()) {
                return true;
            }
        }
        return false;
    }
}
