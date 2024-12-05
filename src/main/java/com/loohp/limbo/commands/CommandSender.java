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

package com.loohp.limbo.commands;

import java.util.UUID;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import net.md_5.bungee.api.chat.BaseComponent;

public interface CommandSender extends Audience {

    void sendMessage(BaseComponent[] component, UUID uuid);

    void sendMessage(BaseComponent component, UUID uuid);

    void sendMessage(String message, UUID uuid);

    void sendMessage(BaseComponent[] component);

    void sendMessage(BaseComponent component);

    void sendMessage(String message);

    boolean hasPermission(String permission);

    String getName();

    void sendMessage(Identity source, Component message, MessageType type);

    void openBook(Book book);

    void stopSound(SoundStop stop);

    void playSound(Sound sound, Sound.Emitter emitter);

    void playSound(Sound sound, double x, double y, double z);

    void playSound(Sound sound);

    void sendActionBar(Component message);

    void sendPlayerListHeaderAndFooter(Component header, Component footer);

    <T> void sendTitlePart(TitlePart<T> part, T value);

    void clearTitle();

    void resetTitle();

    void showBossBar(BossBar bar);

    void hideBossBar(BossBar bar);

}
