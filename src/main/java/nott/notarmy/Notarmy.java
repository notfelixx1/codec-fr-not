package nott.notarmy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("notarmy")
public class Notarmy {
    public static final String MODID = "notarmy";

    public Notarmy() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(KeyHandler::registerKeyMappings);
        MinecraftForge.EVENT_BUS.register(this);
    }
}