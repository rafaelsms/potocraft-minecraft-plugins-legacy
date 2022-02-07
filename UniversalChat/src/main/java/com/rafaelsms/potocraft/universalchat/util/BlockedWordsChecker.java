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
                stringBuilder.append(c).append("\\s*");
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
