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
import java.util.Optional;

import com.loohp.limbo.registry.PacketRegistry;
import com.loohp.limbo.utils.DataTypeIO;

import net.kyori.adventure.text.Component;

public class PacketPlayOutTabComplete extends PacketOut {

    private final int id;
    private final int start;
    private final int length;
    private final TabCompleteMatches[] matches;

    public PacketPlayOutTabComplete(int id, int start, int length, TabCompleteMatches... matches) {
        this.id = id;
        this.start = start;
        this.length = length;
        this.matches = matches;
    }

    public int getId() {
        return id;
    }

    public int getStart() {
        return start;
    }

    public int getLength() {
        return length;
    }

    public TabCompleteMatches[] getMatches() {
        return matches;
    }

    @Override
    public byte[] serializePacket() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        DataOutputStream output = new DataOutputStream(buffer);
        output.writeByte(PacketRegistry.getPacketId(getClass()));
        DataTypeIO.writeVarInt(output, id);
        DataTypeIO.writeVarInt(output, start);
        DataTypeIO.writeVarInt(output, length);
        DataTypeIO.writeVarInt(output, matches.length);

        for (TabCompleteMatches match : matches) {
            DataTypeIO.writeString(output, match.getMatch(), StandardCharsets.UTF_8);
            if (match.getTooltip().isPresent()) {
                output.writeBoolean(true);
                DataTypeIO.writeComponent(output, match.getTooltip().get());
            } else {
                output.writeBoolean(false);
            }
        }

        return buffer.toByteArray();
    }

    public static class TabCompleteMatches {

        private final String match;
        private final Optional<Component> tooltip;

        public TabCompleteMatches(String match) {
            this.match = match;
            this.tooltip = Optional.empty();
        }

        public TabCompleteMatches(String match, Component tooltip) {
            this.match = match;
            this.tooltip = Optional.ofNullable(tooltip);
        }

        public String getMatch() {
            return match;
        }

        public Optional<Component> getTooltip() {
            return tooltip;
        }

    }

}
