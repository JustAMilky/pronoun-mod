package com.milky.pronounmod;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(PronounMod.MODID)
public class PronounMod {

    public static final String MODID = "pronounmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PronounMod(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.addListener(PronounCommand::register);

        LOGGER.info("Pronoun Mod by JustAMilky initialised");
    }
}
