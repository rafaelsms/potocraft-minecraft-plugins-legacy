package com.rafaelsms.potocraft.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TextUtilTest {

    private final List<String> playerNames =
            List.of("*bedrockPlayer", "javaPlayer", "fakeBedrockPlayer", "*javaPlayer", "b412_", "b312_");

    @Test
    void testClosestMatchSingle() {
        assertEquals("*bedrockPlayer", TextUtil.closestMatch(playerNames, "bedrockplayer").orElse(null));
        assertEquals("*bedrockPlayer", TextUtil.closestMatch(playerNames, "bedrock").orElse(null));
        assertEquals("*bedrockPlayer", TextUtil.closestMatch(playerNames, "bed").orElse(null));
        assertEquals("*bedrockPlayer", TextUtil.closestMatch(playerNames, "*b").orElse(null));
        assertEquals("fakeBedrockPlayer", TextUtil.closestMatch(playerNames, "f").orElse(null));
        assertEquals("fakeBedrockPlayer", TextUtil.closestMatch(playerNames, "fake").orElse(null));
        assertEquals("b312_", TextUtil.closestMatch(playerNames, "b3").orElse(null));
        assertEquals("javaPlayer", TextUtil.closestMatch(playerNames, "j").orElse(null));
        assertEquals("*javaPlayer", TextUtil.closestMatch(playerNames, "*j").orElse(null));
        assertNull(TextUtil.closestMatch(playerNames, "b").orElse(null));
    }

}
