package org.opencommunity.goodtrade.utils;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Util {
    public Util() {
    }

    /**
     * Sends a message to a player or to the console if the player is null.
     *
     * @param player the player to send the message to, or null to send to console
     * @param messageKey the key of the message to send, as defined in the LocaleAPI
     */
    public static void sendMessage(Player player, String messageKey) {
        if (player != null) {
            player.sendMessage(FormatUtil.replaceFormat(LocaleAPI.getMessage(player, messageKey)));
        } else {
            Bukkit.getConsoleSender().sendMessage(LocaleAPI.getMessage(null, messageKey));
        }
    }

    /**
     * Sends a formatted message to a player or to the console if the player is null.
     * Placeholders in the message are replaced with the corresponding values in the placeholders map.
     *
     * @param player the player to send the message to, or null to send to console
     * @param messageKey the key of the message to send, as defined in the LocaleAPI
     * @param placeholders a map of placeholder names to replacement values
     */
    public static void sendFormattedMessage(Player player, String messageKey, Map<String, String> placeholders) {
        String message = LocaleAPI.getMessage(player, messageKey);

        if (message == null) {
            return;
        }

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replaceAll("%" + entry.getKey(), entry.getValue());
        }

        if (player != null) {
            player.sendMessage(FormatUtil.replaceFormat(message));
        } else {
            Bukkit.getConsoleSender().sendMessage(message);
        }
    }

}
