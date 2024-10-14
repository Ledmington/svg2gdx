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
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ledmington.svg.path.LineTo;
import com.ledmington.svg.path.MoveTo;
import com.ledmington.svg.path.Path;
import com.ledmington.svg.path.Point;
import com.ledmington.svg.path.SubPath;

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
                Arguments.of(
                        load.apply("triangle01.svg"),
                        new Image(
                                new ViewBox(0.0, 0.0, 400.0, 400.0),
                                151.18,
                                151.18,
                                List.of(
                                        new Rectangle(
                                                1.0,
                                                1.0,
                                                398.0,
                                                398.0,
                                                new Color((byte) 0, (byte) 0, (byte) 0, (byte) 0),
                                                new Color((byte) 0, (byte) 0, (byte) 0xff, (byte) 0xff),
                                                0.0),
                                        new Path(
                                                List.of(new SubPath(
                                                        List.of(
                                                                new MoveTo(false, List.of(new Point(100.0, 100.0))),
                                                                new LineTo(false, List.of(new Point(300.0, 100.0))),
                                                                new LineTo(false, List.of(new Point(200.0, 300.0)))))),
                                                new Color((byte) 0xff, (byte) 0, (byte) 0, (byte) 0xff),
                                                new Color((byte) 0, (byte) 0, (byte) 0xff, (byte) 0xff),
                                                3.0)))),
                Arguments.of(load.apply("cubic01.svg"), null),
                Arguments.of(load.apply("quad01.svg"), null),
                Arguments.of(load.apply("arcs01.svg"), null));
    }

    @ParameterizedTest
    @MethodSource("testSVGFiles")
    void testParsing(final File image, final Image expected) {
        final Image actual = Parser.parseImage(image);
        assertEquals(expected, actual, () -> String.format("Expected '%s' but was '%s'.", expected, actual));
    }
}
