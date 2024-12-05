/*
 * This file is part of Limbo.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.loohp.limbo.player;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.loohp.limbo.Limbo;
import com.loohp.limbo.entity.Entity;
import com.loohp.limbo.location.Location;
import com.loohp.limbo.network.ClientConnection;
import com.loohp.limbo.network.protocol.packets.*;
import com.loohp.limbo.world.ChunkPosition;
import com.loohp.limbo.world.World;

import net.querz.mca.Chunk;

public class PlayerInteractManager {

    private Player player;

    private Set<Entity> entities;
    private Map<ChunkPosition, Chunk> currentViewing;

    public PlayerInteractManager() {
        this.player = null;
        this.entities = new HashSet<>();
        this.currentViewing = new HashMap<>();
    }

    public Player getPlayer() {
        return player;
    }

    protected void setPlayer(Player player) {
        if (this.player == null) {
            this.player = player;
        } else {
            throw new RuntimeException("Player in PlayerInteractManager cannot be changed once created");
        }
    }

    public void update() throws IOException {
        if (player.clientConnection.getClientState() != ClientConnection.ClientState.PLAY) {
            return;
        }

        int viewDistanceChunks = Limbo.getInstance().getServerProperties().getViewDistance();
        int viewDistanceBlocks = viewDistanceChunks << 4;
        Location location = player.getLocation();
        Set<Entity> entitiesInRange = player.getWorld().getEntities().stream().filter(each -> each.getLocation().distanceSquared(location) < viewDistanceBlocks * viewDistanceBlocks).collect(Collectors.toSet());
        for (Entity entity : entitiesInRange) {
            if (!entities.contains(entity)) {
                PacketPlayOutSpawnEntity packet = new PacketPlayOutSpawnEntity(entity.getEntityId(), entity.getUniqueId(), entity.getType(), entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch(), entity.getPitch(), 0, (short) 0, (short) 0, (short) 0);
                player.clientConnection.sendPacket(packet);

                PacketPlayOutEntityMetadata meta = new PacketPlayOutEntityMetadata(entity);
                player.clientConnection.sendPacket(meta);
            }
        }
        List<Integer> ids = new ArrayList<>();
        for (Entity entity : entities) {
            if (!entitiesInRange.contains(entity)) {
                ids.add(entity.getEntityId());
            }
        }
        for (int id : ids) {
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(id);
            player.clientConnection.sendPacket(packet);
        }

        entities = entitiesInRange;

        int playerChunkX = (int) location.getX() >> 4;
        int playerChunkZ = (int) location.getZ() >> 4;
        World world = location.getWorld();

        Map<ChunkPosition, Chunk> chunksInRange = new HashMap<>();

        for (int x = playerChunkX - viewDistanceChunks; x < playerChunkX + viewDistanceChunks; x++) {
            for (int z = playerChunkZ - viewDistanceChunks; z < playerChunkZ + viewDistanceChunks; z++) {
                Chunk chunk = world.getChunkAt(x, z);
                if (chunk != null) {
                    chunksInRange.put(new ChunkPosition(world, x, z), chunk);
                } else {
                    chunksInRange.put(new ChunkPosition(world, x, z), World.EMPTY_CHUNK);
                }
            }
        }

        for (Entry<ChunkPosition, Chunk> entry : currentViewing.entrySet()) {
            ChunkPosition chunkPos = entry.getKey();
            if (!chunksInRange.containsKey(chunkPos)) {
                PacketPlayOutUnloadChunk packet = new PacketPlayOutUnloadChunk(chunkPos.getChunkX(), chunkPos.getChunkZ());
                player.clientConnection.sendPacket(packet);
            }
        }

        int counter = 0;
        ClientboundChunkBatchStartPacket chunkBatchStartPacket = new ClientboundChunkBatchStartPacket();
        player.clientConnection.sendPacket(chunkBatchStartPacket);
        for (Entry<ChunkPosition, Chunk> entry : chunksInRange.entrySet()) {
            ChunkPosition chunkPos = entry.getKey();
            if (!currentViewing.containsKey(chunkPos)) {
                Chunk chunk = chunkPos.getWorld().getChunkAt(chunkPos.getChunkX(), chunkPos.getChunkZ());
                if (chunk == null) {
                    ClientboundLevelChunkWithLightPacket chunkdata = new ClientboundLevelChunkWithLightPacket(chunkPos.getChunkX(), chunkPos.getChunkZ(), entry.getValue(), world.getEnvironment(), Collections.emptyList(), Collections.emptyList());
                    player.clientConnection.sendPacket(chunkdata);
                } else {
                    List<Byte[]> blockChunk = world.getLightEngineBlock().getBlockLightBitMask(chunkPos.getChunkX(), chunkPos.getChunkZ());
                    if (blockChunk == null) {
                        blockChunk = new ArrayList<>();
                    }
                    List<Byte[]> skyChunk = null;
                    if (world.hasSkyLight()) {
                        skyChunk = world.getLightEngineSky().getSkyLightBitMask(chunkPos.getChunkX(), chunkPos.getChunkZ());
                    }
                    if (skyChunk == null) {
                        skyChunk = new ArrayList<>();
                    }
                    ClientboundLevelChunkWithLightPacket chunkdata = new ClientboundLevelChunkWithLightPacket(chunkPos.getChunkX(), chunkPos.getChunkZ(), chunk, world.getEnvironment(), skyChunk, blockChunk);
                    player.clientConnection.sendPacket(chunkdata);
                }
                counter++;
            }
        }
        ClientboundChunkBatchFinishedPacket chunkBatchFinishedPacket = new ClientboundChunkBatchFinishedPacket(counter);
        player.clientConnection.sendPacket(chunkBatchFinishedPacket);

        currentViewing = chunksInRange;
    }

}
