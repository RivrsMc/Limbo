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

package com.loohp.limbo.network.protocol.packets;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.loohp.limbo.registry.PacketRegistry;
import com.loohp.limbo.registry.RegistryCustom;
import com.loohp.limbo.utils.DataTypeIO;
import com.loohp.limbo.utils.GameMode;
import com.loohp.limbo.world.Environment;
import com.loohp.limbo.world.World;

import net.kyori.adventure.key.Key;

public class PacketPlayOutRespawn extends PacketOut {

    private final Environment dimension;
    private final World world;
    private final long hashedSeed;
    private final GameMode gamemode;
    private final boolean isDebug;
    private final boolean isFlat;
    private final boolean copyMetaData;

    public PacketPlayOutRespawn(World world, long hashedSeed, GameMode gamemode, boolean isDebug, boolean isFlat, boolean copyMetaData) {
        this.dimension = world.getEnvironment();
        this.world = world;
        this.hashedSeed = hashedSeed;
        this.gamemode = gamemode;
        this.isDebug = isDebug;
        this.isFlat = isFlat;
        this.copyMetaData = copyMetaData;
    }

    public Environment getDimension() {
        return dimension;
    }

    public World getWorld() {
        return world;
    }

    public long getHashedSeed() {
        return hashedSeed;
    }

    public GameMode getGamemode() {
        return gamemode;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public boolean isFlat() {
        return isFlat;
    }

    public boolean isCopyMetaData() {
        return copyMetaData;
    }

    @Override
    public byte[] serializePacket() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        DataOutputStream output = new DataOutputStream(buffer);
        output.writeByte(PacketRegistry.getPacketId(getClass()));

        DataTypeIO.writeVarInt(output, RegistryCustom.DIMENSION_TYPE.indexOf(world.getEnvironment().getKey()));
        DataTypeIO.writeString(output, Key.key(world.getName()).toString(), StandardCharsets.UTF_8);
        output.writeLong(hashedSeed);
        output.writeByte((byte) gamemode.getId());
        output.writeByte((byte) gamemode.getId());
        output.writeBoolean(isDebug);
        output.writeBoolean(isFlat);
        output.writeBoolean(copyMetaData);

        return buffer.toByteArray();
    }

}
