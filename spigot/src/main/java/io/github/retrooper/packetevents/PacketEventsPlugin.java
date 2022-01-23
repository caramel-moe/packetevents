/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2021 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.retrooper.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.impl.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.impl.PacketSendEvent;
import com.github.retrooper.packetevents.manager.npc.NPC;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.protocol.player.GameProfile;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.MojangAPIUtil;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.github.retrooper.packetevents.utils.SpigotDataHelper;
import io.github.retrooper.packetevents.utils.SpigotReflectionUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

public class PacketEventsPlugin extends JavaPlugin {
    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        //Register your listeners
        PacketEvents.getAPI().init();

        PacketEvents.getAPI().getSettings().debug(true).bStats(true);

        PacketListenerAbstract debugListener = new PacketListenerAbstract(PacketListenerPriority.NORMAL, false) {
            @Override
            public void onPacketReceive(PacketReceiveEvent event) {
                if (event.getPlayer() instanceof Player) {
                    Player player = (Player) event.getPlayer();
                    if (event.getPacketType() instanceof PacketType.Play.Client) {
                        PacketType.Play.Client type = (PacketType.Play.Client) event.getPacketType();
                        switch (type) {
                            case CHAT_MESSAGE: {
                                WrapperPlayClientChatMessage chatMessage = new WrapperPlayClientChatMessage(event);
                                String msg = chatMessage.getMessage();
                                String[] sp = msg.split(" ");
                                if (sp[0].equalsIgnoreCase("plzparticles")) {
                                    Vector3f rgb = new Vector3f(0.3f, 0.0f, 0.6f);
                                    Particle particle = new Particle(ParticleTypes.ANGRY_VILLAGER);
                                    Vector3d position = SpigotDataHelper.fromBukkitLocation(player.getLocation()).getPosition().add(0, 2, 0);
                                    WrapperPlayServerParticle particlePacket
                                            = new WrapperPlayServerParticle(particle, true, position,
                                            new Vector3f(0.4f, 0.4f, 0.4f), 0, 25);
                                    PacketEvents.getAPI().getPlayerManager().sendPacket(player, particlePacket);
                                } else if (sp[0].equalsIgnoreCase("plzspawn")) {
                                    String name = sp[1];
                                    UUID uuid = MojangAPIUtil.requestPlayerUUID(name);
                                    player.sendMessage("Spawning " + name + " with UUID " + uuid.toString());
                                    List<TextureProperty> textureProperties = MojangAPIUtil.requestPlayerTextureProperties(uuid);
                                    GameProfile profile = new GameProfile(uuid, name, textureProperties);
                                    Component nameTag = Component.text("Doggy").color(NamedTextColor.GOLD).asComponent();
                                    NPC npc = new NPC(profile, SpigotReflectionUtil.generateEntityId(), null,
                                            NamedTextColor.GOLD,
                                            Component.text("Nice prefix").color(NamedTextColor.GRAY).asComponent(),
                                            Component.text("Nice suffix").color(NamedTextColor.AQUA).asComponent()
                                    );
                                    npc.setLocation(new Location(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch()));
                                    PacketEvents.getAPI().getNPCManager().spawn(event.getChannel(), npc);
                                    player.sendMessage("Successfully spawned " + name);
                                }
/*
                        Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin) PacketEvents.getAPI().getPlugin(),
                                () -> {
                                    player.sendMessage("Turning the NPC into Dqgs!");
                                    UUID dogsUUID = MojangAPIUtil.requestPlayerUUID("");
                                    List<TextureProperty> newTextureProperties = MojangAPIUtil.requestPlayerTextureProperties(dogsUUID);
                                    PacketEvents.getAPI().getNPCManager().changeNPCSkin(npc, dogsUUID, newTextureProperties);
                                    npc.setPrefixName(Component.text("New prefix").color(NamedTextColor.GOLD).asComponent());
                                    PacketEvents.getAPI().getNPCManager().updateNPCNameTag(npc);

                                }, 120L);//120 ticks is 6 seconds*/
                                break;
                            }
                            case PLAYER_FLYING:
                            case PLAYER_POSITION:
                            case PLAYER_ROTATION:
                            case PLAYER_POSITION_AND_ROTATION:
                                WrapperPlayClientPlayerFlying flying
                                        = new WrapperPlayClientPlayerFlying(event);
                                Location location = flying.getLocation();
                                if (flying.hasPositionChanged()) {
                                    player.sendMessage("position: " + location.getPosition());
                                }
                                //System.out.println("Player flying position: " + flying.hasPositionChanged() + ", rotation: " + flying.hasRotationChanged());
                                event.setLastUsedWrapper(null);
                                break;
                        }
                    }
                }
            }

            @Override
            public void onPacketSend(PacketSendEvent event) {
                Player player = event.getPlayer() == null ? null : (Player) event.getPlayer();
                if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
                    WrapperPlayServerSetSlot setSlot = new WrapperPlayServerSetSlot(event);
                    int windowID = setSlot.getWindowId();
                    int slot = setSlot.getSlot();
                    ItemStack item = setSlot.getItem();
                    player.sendMessage("Set slot with window ID: " + windowID + ", slot: " + slot + ", item: " + (item.getType() != null ? item.toString() : "null item"));
                }
            }
        };

        // net.minecraft.server.v1_7_R4.PacketPlayOutWorldParticles w1;
        //PacketEvents.getAPI().getEventManager().registerListener(debugListener);
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }
}