package com.rafaelsms.potocraft.papermc;

import com.rafaelsms.potocraft.common.Settings;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class PaperSettings extends Settings {

    private final @NotNull PaperPlugin plugin;

    public PaperSettings(@NotNull PaperPlugin plugin) throws Exception {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    protected void setDefaults() {
        super.setDefaults();
        setDefault(Constants.PAPER_SERVER_NAME_ON_PROXY, "<FILL THIS IN OR LOCATIONS WONT BE RESTORED>");

        setDefault(Constants.LOCAL_CHAT_FORMAT, "&e%prefix%%username%%suffix% &f%message%");
        setDefault(Constants.GLOBAL_CHAT_FORMAT, "&7! <&e%prefix%%username%%suffix%&7> &f%message%");
        setDefault(Constants.GLOBAL_CHAT_LIMITER_MESSAGES_AMOUNT, 3);
        setDefault(Constants.GLOBAL_CHAT_LIMITER_TIME_AMOUNT, 5000);
        setDefault(Constants.GLOBAL_CHAT_COMPARATOR_MIN_LENGTH, 3);
        setDefault(Constants.GLOBAL_CHAT_COMPARATOR_THRESHOLD, 3);
    }

    public String getServerName() {
        return get(Constants.PAPER_SERVER_NAME_ON_PROXY);
    }

    public Component getGlobalChatFormat() {
        return getLang(Constants.LOCAL_CHAT_FORMAT);
    }

    public String getGlobalChatPrefix() {
        return get(Constants.GLOBAL_CHAT_FORMAT);
    }

    public int getGlobalChatLimiterMessageAmount() {
        return get(Constants.GLOBAL_CHAT_LIMITER_MESSAGES_AMOUNT);
    }

    public long getGlobalChatLimiterTimeAmount() {
        return get(Constants.GLOBAL_CHAT_LIMITER_TIME_AMOUNT);
    }

    public int getGlobalChatComparatorThreshold() {
        return get(Constants.GLOBAL_CHAT_COMPARATOR_THRESHOLD);
    }

    public int getGlobalChatComparatorMinLength() {
        return get(Constants.GLOBAL_CHAT_COMPARATOR_MIN_LENGTH);
    }
}
