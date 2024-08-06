package com.blud.phenomenon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = MainMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModSounds {

    @ObjectHolder(MainMod.MODID + ":phen_228")
    public static final SoundEvent PHEN_228_SOUND = null;

    @SubscribeEvent
    public static void onRegisterSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(
            new SoundEvent(new ResourceLocation(MainMod.MODID, "phen_228")).setRegistryName(MainMod.MODID, "phen_228")
        );
    }
}