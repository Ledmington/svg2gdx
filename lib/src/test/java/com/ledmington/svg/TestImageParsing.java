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

import java.io.File;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public final class TestImageParsing {

    private static Stream<Arguments> testSVGFiles() {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final Function<String, File> load = s -> {
            try {
                return new File(Objects.requireNonNull(cl.getResource(s)).toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        };
        return Stream.of(
                Arguments.of(load.apply("triangle01.svg"), null),
                Arguments.of(load.apply("cubic01.svg"), null),
                Arguments.of(load.apply("quad01.svg"), null),
                Arguments.of(load.apply("arcs01.svg"), null));
    }

    @ParameterizedTest
    @MethodSource("testSVGFiles")
    void testParsing(final File image, final SVGImage expected) {
        assertEquals(expected, SVGParser.parseImage(image));
    }
}
