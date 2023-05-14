package com.opencommunity.goodtrade.utils;

import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Util {
    public Util() {
    }

    public static void sendMessage(Player player, String messageKey) {
        if (player != null) {
            player.sendMessage(FormatUtil.replaceFormat(LocaleAPI.getMessage(player, messageKey)));
        } else {
            Bukkit.getConsoleSender().sendMessage(LocaleAPI.getMessage((Player)null, messageKey));
        }

    }

    public static void sendFormattedMessage(Player player, String messageKey, Map<String, String> placeholders) {
        String message = LocaleAPI.getMessage(player, messageKey);
        if (message != null) {
            Map.Entry entry;
            for(Iterator var4 = placeholders.entrySet().iterator(); var4.hasNext(); message = message.replaceAll("%" + (String)entry.getKey(), (String)entry.getValue())) {
                entry = (Map.Entry)var4.next();
            }

            if (player != null) {
                player.sendMessage(FormatUtil.replaceFormat(message));
            } else {
                Bukkit.getConsoleSender().sendMessage(message);
            }
        }

    }
}
