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
package com.ledmington.svg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public final class TestParseUtils {

    private static Stream<Arguments> hexByteValues() {
        return IntStream.range(0, 256).mapToObj(x -> Arguments.of(String.format("%02x", x), (byte) (x & 0x000000ff)));
    }

    @ParameterizedTest
    @MethodSource("hexByteValues")
    void parseHexadecimalByte(final String hexString, final byte value) {
        assertEquals(value, ParseUtils.parseByteHex(hexString));
    }

    private static Stream<Arguments> byteToFloatValues() {
        return IntStream.range(0, 256).mapToObj(x -> Arguments.of((byte) (x & 0x000000ff), ((float) x) / 255.0f));
    }

    @ParameterizedTest
    @MethodSource("byteToFloatValues")
    void convertByteToFloat(final byte before, final float expected) {
        final float actual = ParseUtils.byteToFloat(before);
        assertEquals(
                expected,
                actual,
                () -> String.format("Expected 0x%02x to be parsed into %.3f but was %.3f", before, expected, actual));
    }
}
