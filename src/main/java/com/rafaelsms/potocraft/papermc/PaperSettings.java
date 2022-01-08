package com.rafaelsms.potocraft.papermc;

import com.rafaelsms.potocraft.common.Plugin;
import com.rafaelsms.potocraft.common.Settings;
import org.jetbrains.annotations.NotNull;

public class PaperSettings extends Settings {

    public PaperSettings(@NotNull Plugin plugin) throws Exception {
        super(plugin);
    }

    @Override
    protected void setDefaults() {
        super.setDefaults();
        setDefault(Constants.PAPER_SERVER_NAME_ON_PROXY, "");
    }

    public String getServerName() {
        return get(Constants.PAPER_SERVER_NAME_ON_PROXY);
    }

}
