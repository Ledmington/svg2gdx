/*
 * svg2gdx - A converter from SVG to LibGDX ShapeRenderer code.
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
package com.ledmington.svg2gdx.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.ledmington.svg2gdx.SVGElement;

public final class SVGPath implements SVGElement {

    private final List<SVGPathElement> elements;
    private final String colorName;

    public SVGPath(final String pathString, final String colorName) {
        this.elements = new ArrayList<>();
        this.colorName = Objects.requireNonNull(colorName);

        final String[] pathData = pathString.split(" ");
        for (final String pathDatum : pathData) {
            elements.add(
                    switch (pathDatum) {
                            // "moveto" commands
                            // (https://www.w3.org/TR/SVG2/paths.html#PathDataMovetoCommands)
                        case "m" -> new SVGPathMovetoRelative();
                        case "M" -> new SVGPathMovetoAbsolute();

                            // "Bezier" commands
                            // (https://www.w3.org/TR/SVG2/paths.html#PathDataCubicBezierCommands)
                        case "c" -> new SVGPathBezierRelative();
                        case "C" -> new SVGPathBezierAbsolute();

                            // "lineto" commands
                            // (https://www.w3.org/TR/SVG2/paths.html#PathDataLinetoCommands)
                        case "l" -> new SVGPathLinetoRelative();
                        case "L" -> new SVGPathLinetoAbsolute();

                            // "closepath" commands
                            // (https://www.w3.org/TR/SVG2/paths.html#PathDataClosePathCommand)
                        case "z", "Z" -> new SVGPathClosepath();
                        default -> {
                            if (pathDatum.contains(",")) {
                                final String[] data = pathDatum.split(",");
                                yield new SVGPathPoint(Double.parseDouble(data[0]), Double.parseDouble(data[1]));
                            }
                            throw new IllegalArgumentException("Unexpected value: " + pathDatum);
                        }
                    });
        }
    }

    @Override
    public String toGDXShapeRenderer() {
        final StringBuilder sb = new StringBuilder();
        /*
         * if (filled) {
         * sb.append("sr.begin(ShapeType.Filled);\n");
         * } else {
         * sb.append("sr.begin(ShapeType.Line);\n");
         * }
         */
        sb.append(String.format("sr.setColor(%s);\n", colorName));

        SVGPathPoint initialPoint = null;
        int i = 0;
        while (i < elements.size()) {
            final SVGPathElement elem = elements.get(i);
            switch (elem) {
                case SVGPathMovetoAbsolute ignored -> {
                    i++;
                    final SVGPathPoint current = (SVGPathPoint) elements.get(i++);
                    sb.append(String.format("currentX=%sf;\ncurrentY=%sf;\n", current.x(), current.y()));
                    if (initialPoint == null) {
                        initialPoint = current;
                        sb.append(String.format("initialX=%sf;\ninitialY=%sf;\n", current.x(), current.y()));
                    }
                    while (elements.get(i) instanceof SVGPathPoint) {
                        final SVGPathPoint next = (SVGPathPoint) elements.get(i++);
                        sb.append(String.format("sr.line(currentX,currentY,%sf,%sf);\n", next.x(), next.y()));
                        sb.append(String.format("currentX=%sf;\ncurrentY=%sf;\n", next.x(), next.y()));
                    }
                }
                case SVGPathMovetoRelative ignored -> {
                    i++;
                    final SVGPathPoint current = (SVGPathPoint) elements.get(i++);
                    sb.append(String.format("currentX=%sf;\ncurrentY=%sf;\n", current.x(), current.y()));
                    if (initialPoint == null) {
                        initialPoint = current;
                        sb.append(String.format("initialX=%sf;\ninitialY=%sf;\n", current.x(), current.y()));
                    }
                    while (elements.get(i) instanceof SVGPathPoint) {
                        final SVGPathPoint next = (SVGPathPoint) elements.get(i++);
                        sb.append(String.format(
                                "sr.line(currentX,currentY,currentX+%sf,currentY+%sf);\n", next.x(), next.y()));
                        sb.append(String.format("currentX=%sf;\ncurrentY=%sf;\n", next.x(), next.y()));
                    }
                }
                case SVGPathLinetoAbsolute ignored -> {
                    i++;
                    final SVGPathPoint current = (SVGPathPoint) elements.get(i++);
                    sb.append(String.format("currentX=%sf;\ncurrentY=%sf;\n", current.x(), current.y()));
                    while (elements.get(i) instanceof SVGPathPoint) {
                        final SVGPathPoint next = (SVGPathPoint) elements.get(i++);
                        sb.append(String.format("sr.line(currentX,currentY,%sf,%sf);\n", next.x(), next.y()));
                        sb.append(String.format("currentX=%sf;\ncurrentY=%sf;\n", next.x(), next.y()));
                    }
                }
                case SVGPathLinetoRelative ignored -> {
                    i++;
                    final SVGPathPoint current = (SVGPathPoint) elements.get(i++);
                    sb.append(String.format("currentX=%sf;\ncurrentY=%sf;\n", current.x(), current.y()));
                    while (elements.get(i) instanceof SVGPathPoint) {
                        final SVGPathPoint next = (SVGPathPoint) elements.get(i++);
                        sb.append(String.format(
                                "sr.line(currentX,currentY,currentX+%sf,currentY+%sf);\n", next.x(), next.y()));
                        sb.append(String.format("currentX=%sf;\ncurrentY=%sf;\n", next.x(), next.y()));
                    }
                }
                case SVGPathBezierAbsolute ignored -> {
                    i++;
                    while (elements.get(i) instanceof SVGPathPoint) {
                        SVGPathPoint c1 = (SVGPathPoint) elements.get(i++);
                        SVGPathPoint c2 = (SVGPathPoint) elements.get(i++);
                        SVGPathPoint end = (SVGPathPoint) elements.get(i++);
                        sb.append(String.format(
                                "sr.curve(currentX,currentY,%sf,%sf,%sf,%sf,%sf,%sf,50);\n",
                                c1.x(), c1.y(), c2.x(), c2.y(), end.x(), end.y()));
                        sb.append(String.format("currentX=%sf;\ncurrentY=%sf;\n", end.x(), end.y()));
                    }
                }
                case SVGPathBezierRelative ignored -> {
                    i++;
                    while (elements.get(i) instanceof SVGPathPoint) {
                        SVGPathPoint c1 = (SVGPathPoint) elements.get(i++);
                        SVGPathPoint c2 = (SVGPathPoint) elements.get(i++);
                        SVGPathPoint end = (SVGPathPoint) elements.get(i++);
                        sb.append(String.format(
                                "sr.curve(currentX,currentY,currentX+%sf,currentY+%sf,currentX+%sf,currentY+%sf,currentX+%sf,currentY+%sf,50);\n",
                                c1.x(), c1.y(), c2.x(), c2.y(), end.x(), end.y()));
                        sb.append(String.format("currentX=%sf;\ncurrentY=%sf;\n", end.x(), end.y()));
                    }
                }
                case SVGPathClosepath ignored -> {
                    sb.append("sr.line(currentX,currentY,initialX,initialY);\n");
                    sb.append("initialX=0.0f;\ninitialY=0.0f;\n");
                    initialPoint = null;
                    sb.append("currentX=0.0f;\ncurrentY=0.0f;\n");
                    i++;
                }
                default -> throw new IllegalArgumentException(
                        String.format("Unknown SVG path element type '%s'", elem));
            }
        }

        // sb.append("sr.end();\n");
        return sb.toString();
    }
}
