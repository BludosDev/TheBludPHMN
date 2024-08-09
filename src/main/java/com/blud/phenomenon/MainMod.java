package com.blud.phenomenon;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket.Action;
import net.minecraft.server.players.PlayerList;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.EntityType;

@Mod(MainMod.MODID)
public class MainMod {
    public static final String MODID = "bludmod";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> playersJoinedBefore = new HashSet<>();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Block> BLUD_BLOCK = BLOCKS.register("blud_block", () -> new Block(BlockBehaviour.Properties.of(Material.STONE)));
    public static final RegistryObject<Item> BLUD_BLOCK_ITEM = ITEMS.register("blud_block", () -> new BlockItem(BLUD_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS)));

    public MainMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        ModSounds.register(modEventBus);

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
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        String playerName = player.getName().getString();

        if (!playersJoinedBefore.contains(playerName)) {
            player.level.playSound(null, player.getOnPos(), ModSounds.PHEN_228_SOUND.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            playersJoinedBefore.add(playerName);
        }

        // Schedule the fake player join event
        if (player.level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel) player.level;
            MinecraftServer server = serverLevel.getServer();

            serverLevel.getServer().execute(() -> {
                Random random = new Random();
                int delay = 30 + random.nextInt(11); // 30-40 seconds delay

                serverLevel.getServer().getScheduler().schedule(() -> {
                    // Send the fake join message
                    Component joinMessage = new TextComponent("Dhandu joined the game").withStyle(style -> style.withColor(0xFFFF55)); // Yellow color
                    server.getPlayerList().broadcastMessage(joinMessage, false);

                    // Add the fake player to the tab list
                    UUID dhanduUUID = UUID.randomUUID();
                    ServerPlayer fakePlayer = new ServerPlayer(server, serverLevel, new com.mojang.authlib.GameProfile(dhanduUUID, "Dhandu"));
                    fakePlayer.gameMode.setGameModeForPlayer(GameType.SURVIVAL);

                    PlayerList playerList = server.getPlayerList();
                    playerList.getPlayers().forEach(p -> {
                        ServerGamePacketListenerImpl connection = p.connection;
                        connection.send(new ClientboundPlayerInfoPacket(Action.ADD_PLAYER, fakePlayer));
                    });

                }, delay * 20L, java.util.concurrent.TimeUnit.MILLISECONDS);
            });
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        Level level = player.level;

        BlockPos playerPos = player.getOnPos();

        int lightLevel = level.getMaxLocalRawBrightness(playerPos);

        if (lightLevel < 5) {
            Random random = new Random();
            if (random.nextFloat() < 0.01) { // 1% chance to play the sound on each tick
                level.playSound(null, playerPos, ModSounds.BLOCKBREAK_SOUND.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("Blud fornando");
            LOGGER.info("Bludos >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
