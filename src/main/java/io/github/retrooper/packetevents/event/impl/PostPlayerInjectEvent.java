/*
 * MIT License
 *
 * Copyright (c) 2020 retrooper
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.retrooper.packetevents.event.impl;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.event.PacketEvent;
import io.github.retrooper.packetevents.event.PacketListenerDynamic;
import io.github.retrooper.packetevents.event.eventtypes.PlayerEvent;
import io.github.retrooper.packetevents.utils.netty.channel.ChannelUtils;
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

/**
 * The {@code PostPlayerInjectEvent} event is fired after a successful injection.
 * If you are on an older version of PacketEvents DON'T use this to register player data.
 * This event might be called asynchronously and sometimes synchronously.
 * Use the {@link #isAsync()} method to figure out if is being called sync or async.
 * Make sure you do null checks in your packet listeners as this might be called a bit later.
 * A player is injected by PacketEvents whenever they join the server.
 *
 * @author retrooper
 * @see <a href="https://github.com/retrooper/packetevents/blob/dev/src/main/java/io/github/retrooper/packetevents/handler/PacketHandlerInternal.java">https://github.com/retrooper/packetevents/blob/dev/src/main/java/io/github/retrooper/packetevents/handler/PacketHandlerInternal.java</a>
 * @since 1.3
 */
public class PostPlayerInjectEvent extends PacketEvent implements PlayerEvent {
    private final Player player;
    private final boolean async;

    public PostPlayerInjectEvent(Player player, boolean async) {
        this.player = player;
        this.async = async;
    }

    /**
     * This method returns the bukkit player object of the player that has been injected.
     * The player is guaranteed to not be null.
     *
     * @return Injected Player.
     */
    @NotNull
    @Override
    public Player getPlayer() {
        return player;
    }

    /**
     * This method returns the cached netty channel of the player.
     *
     * @return Netty channel of the injected player.
     */
    @NotNull
    public Object getChannel() {
        return PacketEvents.get().packetProcessorInternal.getChannel(player);
    }

    @NotNull
    public InetSocketAddress getSocketAddress() {
        return ChannelUtils.getSocketAddress(getChannel());
    }

    /**
     * This method returns the ClientVersion of the injected player.
     *
     * @return ClientVersion of injected player.
     * @see ClientVersion
     */
    @NotNull
    public ClientVersion getClientVersion() {
        return PacketEvents.get().getPlayerUtils().getClientVersion(player);
    }

    /**
     * Has the event been called async or sync?
     *
     * @return Was the event call in an async context?
     */
    public boolean isAsync() {
        return async;
    }

    @Override
    public void call(PacketListenerDynamic listener) {
        listener.onPostPlayerInject(this);
    }

    @Override
    public boolean isInbuilt() {
        return true;
    }
}
