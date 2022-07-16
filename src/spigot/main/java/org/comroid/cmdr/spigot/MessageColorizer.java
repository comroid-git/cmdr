package org.comroid.cmdr.spigot;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.ApiStatus.Internal;

public interface MessageColorizer {
    ChatColor getPrimaryColor();
    ChatColor getSecondaryColor();
    ChatColor getDecorationColor();

    default String makeMessage(String format, Object... vars) {
        return getPrimaryColor() + String.format(format, (Object[]) formatStrings(this, vars));
    }

    @Internal
    static String[] formatStrings(MessageColorizer colorizer, Object[] vars) {
        String[] strings = new String[vars.length];

        for (int i = 0; i < vars.length; i++)
            strings[i] = colorizer.getSecondaryColor() + String.valueOf(vars[i]) + colorizer.getPrimaryColor();

        return strings;
    }

    class Impl implements MessageColorizer {
        private final ChatColor primaryColor;
        private final ChatColor secondaryColor;
        private final ChatColor decorationColor;

        @Override
        public ChatColor getPrimaryColor() {
            return primaryColor;
        }

        @Override
        public ChatColor getSecondaryColor() {
            return secondaryColor;
        }

        @Override
        public ChatColor getDecorationColor() {
            return decorationColor;
        }

        public Impl(ChatColor primaryColor, ChatColor secondaryColor) {
            this(primaryColor, secondaryColor, ChatColor.GRAY);
        }

        public Impl(ChatColor primaryColor, ChatColor secondaryColor, ChatColor decorationColor) {
            this.primaryColor = primaryColor;
            this.secondaryColor = secondaryColor;
            this.decorationColor = decorationColor;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Impl impl = (Impl) o;

            if (getPrimaryColor() != impl.getPrimaryColor()) return false;
            if (getSecondaryColor() != impl.getSecondaryColor()) return false;
            return getDecorationColor() == impl.getDecorationColor();
        }

        @Override
        public int hashCode() {
            int result = getPrimaryColor().hashCode();
            result = 31 * result + getSecondaryColor().hashCode();
            result = 31 * result + getDecorationColor().hashCode();
            return result;
        }
    }
}
