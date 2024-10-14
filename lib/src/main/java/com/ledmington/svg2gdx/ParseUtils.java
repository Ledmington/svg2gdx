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

import java.util.Objects;

/** Collection of utility methods for parsing. */
public final class ParseUtils {

    private ParseUtils() {}

    /**
     * Parses the given {@link String} as an hexadecimal 2-digit byte value.
     *
     * <p>Examples: "0a" -> 10.
     *
     * @param s The 2-char hex string to be converted.
     * @return The byte represented by this hex string.
     */
    public static byte parseByteHex(final String s) {
        Objects.requireNonNull(s);
        if (s.length() != 2) {
            throw new IllegalArgumentException(
                    String.format("Invalid String: expected length 2 but was %,d", s.length()));
        }

        return or(shl(parseByteHex(s.charAt(0)), 4), parseByteHex(s.charAt(1)));
    }

    private static byte parseByteHex(final char ch) {
        return switch (ch) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> (byte) ((ch - '0') & 0x000000ff);
            case 'a', 'b', 'c', 'd', 'e', 'f' -> (byte) ((ch - 'a' + 10) & 0x000000ff);
            case 'A', 'B', 'C', 'D', 'E', 'F' -> (byte) ((ch - 'A' + 10) & 0x000000ff);
            default -> throw new IllegalArgumentException(String.format("Unknown hexadecimal character '%c'", ch));
        };
    }

    /**
     * Returns the given 4-byte integer's rightmost byte.
     *
     * @param x The integer to be converted.
     * @return The rightmost byte.
     */
    public static byte asByte(final int x) {
        return (byte) (x & 0x000000ff);
    }

    /**
     * Computes the bitwise OR between the given arguments and returns it as a byte.
     *
     * @param a The left hand side.
     * @param b The right hand side.
     * @return The bitwise OR as a byte.
     */
    public static byte or(final byte a, final byte b) {
        return asByte(a | b);
    }

    /**
     * Shifts the given byte to the left by the given amount and returns it as a byte.
     *
     * @param x The byte to be shifted.
     * @param n The number of bits to shfit.
     * @return The shifted value as a byte.
     */
    public static byte shl(final byte x, final int n) {
        return asByte(x << n);
    }

    /**
     * Converts an unsigned byte into a float in [0;1].
     *
     * @param x The unsigned byte to be converted.
     * @return A float mapped in [0;1].
     */
    public static float byteToFloat(final byte x) {
        final double y = x >= 0 ? (double) x : (double) x + 256.0;
        return (float) (y / 255.0);
    }
}
