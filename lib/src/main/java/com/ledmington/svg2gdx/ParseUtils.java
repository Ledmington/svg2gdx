/*
 * svg2gdx - A converter from SVG to libGDX ShapeRenderer code.
 * Copyright (C) 2023-2024 Filippo Barbari <filippo.barbari@gmail.com>
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
package com.ledmington.svg2gdx;

public final class ParseUtils {
    private ParseUtils() {}

    /** Parses the given {@link String} as an hexadecimal 2-digit byte value. */
    public static byte parseByteHex(final String s) {
        if (s == null) {
            throw new NullPointerException();
        }
        if (s.length() != 2) {
            throw new IllegalArgumentException(
                    String.format("Invalid String: expected length 2 but was %,d", s.length()));
        }

        return (byte) (((parseByteHex(s.charAt(0)) << 4) & 0x000000ff) | (parseByteHex(s.charAt(1)) & 0x000000ff));
    }

    private static byte parseByteHex(final char ch) {
        return switch (ch) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> (byte) ((ch - '0') & 0x000000ff);
            case 'a', 'b', 'c', 'd', 'e', 'f' -> (byte) ((ch - 'a' + 10) & 0x000000ff);
            case 'A', 'B', 'C', 'D', 'E', 'F' -> (byte) ((ch - 'A' + 10) & 0x000000ff);
            default -> throw new IllegalArgumentException(String.format("Unknown hexadecimal character '%c'", ch));
        };
    }
}
