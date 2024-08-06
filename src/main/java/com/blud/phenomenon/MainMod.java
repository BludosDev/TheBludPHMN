package com.blud.phenomenon;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(MainMod.MODID)
public class MainMod {
    public static final String MODID = "bludmod";
    private static final Logger LOGGER = LogUtils.getLogger();
    private boolean hasPlayedSoundTonight = false; // Tracks if the sound has been played this night

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Block> BLUD_BLOCK = BLOCKS.register("blud_block", () -> new Block(BlockBehaviour.Properties.of(Material.STONE)));
    public static final RegistryObject<Item> BLUD_BLOCK_ITEM = ITEMS.register("blud_block", () -> new BlockItem(BLUD_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS)));

    public MainMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        ModSounds.register(modEventBus);  // Register the sound events

        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Blud Phenomenon Started Iguess");

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Yo bro is running my mod in server");
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!event.player.level.isClientSide) {  // Ensure it runs only on the server side
            Player player = event.player;
            long time = player.level.getDayTime() % 24000;

            if (time >= 13000 && time <= 23000) { // Check if it is night time
                boolean isNearBed = false;
                BlockPos playerPos = player.getOnPos();

                for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(playerPos.getX() - 3, playerPos.getY() - 3, playerPos.getZ() - 3), new BlockPos(playerPos.getX() + 3, playerPos.getY() + 3, playerPos.getZ() + 3))) {
                    if (player.level.getBlockState(pos).getBlock() instanceof BedBlock) {
                        isNearBed = true;
                        break;
                    }
                }

                if (isNearBed && !hasPlayedSoundTonight) {
                    player.level.playSound(null, player.getOnPos(), ModSounds.PHEN_228_SOUND.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                    hasPlayedSoundTonight = true; // Mark that the sound has been played this night
                }
            } else {
                // Reset the flag when day starts
                hasPlayedSoundTonight = false;
            }
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("Yo Pigga");
            LOGGER.info("Bludos >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
