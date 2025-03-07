/*
 * svg2gdx - A converter from SVG to libGDX ShapeRenderer code.
 * Copyright (C) 2023-2025 Filippo Barbari <filippo.barbari@gmail.com>
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

import com.ledmington.svg.path.Arc;
import com.ledmington.svg.path.ArcElement;
import com.ledmington.svg.path.CubicBezier;
import com.ledmington.svg.path.CubicBezierElement;
import com.ledmington.svg.path.HorizontalLineTo;
import com.ledmington.svg.path.LineTo;
import com.ledmington.svg.path.MoveTo;
import com.ledmington.svg.path.Path;
import com.ledmington.svg.path.Point;
import com.ledmington.svg.path.QuadraticBezier;
import com.ledmington.svg.path.QuadraticBezierElement;
import com.ledmington.svg.path.SmoothCubicBezier;
import com.ledmington.svg.path.SmoothCubicBezierElement;
import com.ledmington.svg.path.SmoothQuadraticBezier;
import com.ledmington.svg.path.SmoothQuadraticBezierElement;
import com.ledmington.svg.path.SubPath;
import com.ledmington.svg.path.VerticalLineTo;

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
                                                new Color(),
                                                new Color((byte) 0, (byte) 0, (byte) 0xff, (byte) 0xff),
                                                1.0),
                                        new Path(
                                                List.of(new SubPath(
                                                        List.of(
                                                                new MoveTo(false, List.of(new Point(100.0, 100.0))),
                                                                new LineTo(false, List.of(new Point(300.0, 100.0))),
                                                                new LineTo(false, List.of(new Point(200.0, 300.0)))))),
                                                new Color((byte) 0xff, (byte) 0, (byte) 0, (byte) 0xff),
                                                new Color((byte) 0, (byte) 0, (byte) 0xff, (byte) 0xff),
                                                3.0)))),
                Arguments.of(
                        load.apply("cubic01.svg"),
                        new Image(
                                new ViewBox(0.0, 0.0, 500.0, 400.0),
                                188.97500000000002,
                                151.18,
                                List.of(
                                        new Rectangle(1.0, 1.0, 498.0, 398.0, new Color(), new Color(), 1.0),
                                        new Polyline(List.of(new Point(100.0, 200.0), new Point(100.0, 100.0))),
                                        new Polyline(List.of(new Point(250.0, 100.0), new Point(250.0, 200.0))),
                                        new Polyline(List.of(new Point(250.0, 200.0), new Point(250.0, 300.0))),
                                        new Polyline(List.of(new Point(400.0, 300.0), new Point(400.0, 200.0))),
                                        new Path(
                                                List.of(new SubPath(
                                                        List.of(
                                                                new MoveTo(false, List.of(new Point(100.0, 200.0))),
                                                                new CubicBezier(
                                                                        false,
                                                                        List.of(
                                                                                new CubicBezierElement(
                                                                                        new Point(100.0, 100.0),
                                                                                        new Point(250.0, 100.0),
                                                                                        new Point(250.0, 200.0)))),
                                                                new SmoothCubicBezier(
                                                                        false,
                                                                        List.of(
                                                                                new SmoothCubicBezierElement(
                                                                                        new Point(400.0, 300.0),
                                                                                        new Point(400.0, 200.0))))))),
                                                new Color(),
                                                new Color(),
                                                1.0),
                                        new Circle(100.0, 200.0, 10.0),
                                        new Circle(250.0, 200.0, 10.0),
                                        new Circle(400.0, 200.0, 10.0),
                                        new Circle(100.0, 100.0, 10.0),
                                        new Circle(250.0, 100.0, 10.0),
                                        new Circle(400.0, 300.0, 10.0),
                                        new Circle(250.0, 300.0, 9.0)))),
                Arguments.of(
                        load.apply("quad01.svg"),
                        new Image(
                                new ViewBox(0.0, 0.0, 1200.0, 600.0),
                                453.54,
                                226.77,
                                List.of(
                                        new Rectangle(
                                                1.0,
                                                1.0,
                                                1198.0,
                                                598.0,
                                                new Color(),
                                                new Color((byte) 0, (byte) 0, (byte) 0xff, (byte) 0xff),
                                                1.0),
                                        new Path(
                                                List.of(new SubPath(
                                                        List.of(
                                                                new MoveTo(false, List.of(new Point(200.0, 300.0))),
                                                                new QuadraticBezier(
                                                                        false,
                                                                        List.of(
                                                                                new QuadraticBezierElement(
                                                                                        new Point(400.0, 50.0),
                                                                                        new Point(600.0, 300.0)))),
                                                                new SmoothQuadraticBezier(
                                                                        false,
                                                                        List.of(
                                                                                new SmoothQuadraticBezierElement(
                                                                                        new Point(1000.0, 300.0))))))),
                                                new Color(),
                                                new Color((byte) 0xff, (byte) 0, (byte) 0, (byte) 0xff),
                                                5.0),
                                        new Group(
                                                new Style(
                                                        new Color((byte) 0, (byte) 0, (byte) 0, (byte) 0xff),
                                                        new Color(),
                                                        1.0),
                                                List.of(
                                                        new Circle(200.0, 300.0, 10.0),
                                                        new Circle(600.0, 300.0, 10.0),
                                                        new Circle(1000.0, 300.0, 10.0))),
                                        new Group(
                                                new Style(
                                                        new Color((byte) 0x88, (byte) 0x88, (byte) 0x88, (byte) 0xff),
                                                        new Color(),
                                                        1.0),
                                                List.of(new Circle(400.0, 50.0, 10.0), new Circle(800.0, 550.0, 10.0))),
                                        new Path(
                                                List.of(new SubPath(
                                                        List.of(
                                                                new MoveTo(false, List.of(new Point(200.0, 300.0))),
                                                                new LineTo(false, List.of(new Point(400.0, 50.0))),
                                                                new LineTo(false, List.of(new Point(600.0, 300.0))),
                                                                new LineTo(false, List.of(new Point(800.0, 550.0))),
                                                                new LineTo(false, List.of(new Point(1000.0, 300.0)))))),
                                                new Color(),
                                                new Color((byte) 0x88, (byte) 0x88, (byte) 0x88, (byte) 0xff),
                                                2.0)))),
                Arguments.of(
                        load.apply("arcs01.svg"),
                        new Image(
                                new ViewBox(0.0, 0.0, 1200.0, 400.0),
                                453.54,
                                198.42375,
                                List.of(
                                        new Rectangle(
                                                1.0,
                                                1.0,
                                                1198.0,
                                                398.0,
                                                new Color(),
                                                new Color((byte) 0, (byte) 0, (byte) 0xff, (byte) 0xff),
                                                1.0),
                                        new Path(
                                                List.of(new SubPath(
                                                        List.of(
                                                                new MoveTo(false, List.of(new Point(300.0, 200.0))),
                                                                new HorizontalLineTo(true, List.of(-150.0)),
                                                                new Arc(
                                                                        true,
                                                                        List.of(
                                                                                new ArcElement(
                                                                                        150.0, 150.0, 0.0, 1.0, 0.0,
                                                                                        150.0, -150.0)))))),
                                                new Color((byte) 0xff, (byte) 0, (byte) 0, (byte) 0xff),
                                                new Color((byte) 0, (byte) 0, (byte) 0xff, (byte) 0xff),
                                                5.0),
                                        new Path(
                                                List.of(new SubPath(
                                                        List.of(
                                                                new MoveTo(false, List.of(new Point(275.0, 175.0))),
                                                                new VerticalLineTo(true, List.of(-150.0)),
                                                                new Arc(
                                                                        true,
                                                                        List.of(
                                                                                new ArcElement(
                                                                                        150.0, 150.0, 0.0, 0.0, 0.0,
                                                                                        -150.0, 150.0)))))),
                                                new Color((byte) 0xff, (byte) 0xff, (byte) 0, (byte) 0xff),
                                                new Color((byte) 0, (byte) 0, (byte) 0xff, (byte) 0xff),
                                                5.0),
                                        new Path(
                                                List.of(new SubPath(
                                                        List.of(
                                                                new MoveTo(false, List.of(new Point(600.0, 350.0))),
                                                                new LineTo(true, List.of(new Point(50.0, -25.0))),
                                                                new Arc(
                                                                        true,
                                                                        List.of(
                                                                                new ArcElement(
                                                                                        25.0, 25.0, -30.0, 0.0, 1.0,
                                                                                        50.0, -25.0))),
                                                                new LineTo(true, List.of(new Point(50.0, -25.0))),
                                                                new Arc(
                                                                        true,
                                                                        List.of(
                                                                                new ArcElement(
                                                                                        25.0, 50.0, -30.0, 0.0, 1.0,
                                                                                        50.0, -25.0))),
                                                                new LineTo(true, List.of(new Point(50.0, -25.0))),
                                                                new Arc(
                                                                        true,
                                                                        List.of(
                                                                                new ArcElement(
                                                                                        25.0, 75.0, -30.0, 0.0, 1.0,
                                                                                        50.0, -25.0))),
                                                                new LineTo(true, List.of(new Point(50.0, -25.0))),
                                                                new Arc(
                                                                        true,
                                                                        List.of(
                                                                                new ArcElement(
                                                                                        25.0, 100.0, -30.0, 0.0, 1.0,
                                                                                        50.0, -25.0))),
                                                                new LineTo(true, List.of(new Point(50.0, -25.0)))))),
                                                new Color(),
                                                new Color((byte) 0xff, (byte) 0, (byte) 0, (byte) 0xff),
                                                5.0)))));
    }

    @ParameterizedTest
    @MethodSource("testSVGFiles")
    void testParsing(final File image, final Image expected) {
        final Image actual = Parser.parseImage(image);
        assertEquals(expected, actual, () -> String.format("Expected '%s' but was '%s'.", expected, actual));
    }
}
