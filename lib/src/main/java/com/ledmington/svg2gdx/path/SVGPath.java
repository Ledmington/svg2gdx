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
        sb.append(String.format("sr.setColor(%s);", colorName)).append('\n');

        SVGPathPoint initialPoint = null;
        int i = 0;
        while (i < elements.size()) {
            final SVGPathElement elem = elements.get(i);
            switch (elem) {
                case SVGPathMovetoAbsolute ignored -> {
                    i++;
                    final SVGPathPoint current = (SVGPathPoint) elements.get(i++);
                    sb.append("currentX=")
                            .append(current.x())
                            .append("f;\ncurrentY=")
                            .append(current.y())
                            .append("f;\n");
                    if (initialPoint == null) {
                        initialPoint = current;
                        sb.append("initialX=")
                                .append(current.x())
                                .append("f;\ninitialY=")
                                .append(current.y())
                                .append("f;\n");
                    }
                    while (elements.get(i) instanceof SVGPathPoint) {
                        final SVGPathPoint next = (SVGPathPoint) elements.get(i++);
                        sb.append(String.format("sr.line(currentX,currentY,%sf,%sf);", next.x(), next.y()))
                                .append('\n')
                                .append("currentX=")
                                .append(next.x())
                                .append("f;\ncurrentY=")
                                .append(next.y())
                                .append("f;\n");
                    }
                }
                case SVGPathMovetoRelative ignored -> {
                    i++;
                    final SVGPathPoint current = (SVGPathPoint) elements.get(i++);
                    sb.append("currentX=")
                            .append(current.x())
                            .append("f;\ncurrentY=")
                            .append(current.y())
                            .append("f;\n");
                    if (initialPoint == null) {
                        initialPoint = current;
                        sb.append("initialX=")
                                .append(current.x())
                                .append("f;\ninitialY=")
                                .append(current.y())
                                .append("f;\n");
                    }
                    while (elements.get(i) instanceof SVGPathPoint) {
                        final SVGPathPoint next = (SVGPathPoint) elements.get(i++);
                        sb.append(String.format(
                                        "sr.line(currentX,currentY,currentX+%sf,currentY+%sf);", next.x(), next.y()))
                                .append('\n')
                                .append("currentX=")
                                .append(next.x())
                                .append("f;\ncurrentY=")
                                .append(next.y())
                                .append("f;\n");
                    }
                }
                case SVGPathLinetoAbsolute ignored -> {
                    i++;
                    final SVGPathPoint current = (SVGPathPoint) elements.get(i++);
                    sb.append("currentX=")
                            .append(current.x())
                            .append("f;\ncurrentY=")
                            .append(current.y())
                            .append("f;\n");
                    while (elements.get(i) instanceof SVGPathPoint) {
                        final SVGPathPoint next = (SVGPathPoint) elements.get(i++);
                        sb.append(String.format("sr.line(currentX,currentY,%sf,%sf);", next.x(), next.y()))
                                .append('\n')
                                .append("currentX=")
                                .append(next.x())
                                .append("f;\ncurrentY=")
                                .append(next.y())
                                .append("f;\n");
                    }
                }
                case SVGPathLinetoRelative ignored -> {
                    i++;
                    final SVGPathPoint current = (SVGPathPoint) elements.get(i++);
                    sb.append("currentX=")
                            .append(current.x())
                            .append("f;\ncurrentY=")
                            .append(current.y())
                            .append("f;\n");
                    while (elements.get(i) instanceof SVGPathPoint) {
                        final SVGPathPoint next = (SVGPathPoint) elements.get(i++);
                        sb.append(String.format(
                                        "sr.line(currentX,currentY,currentX+%sf,currentY+%sf);", next.x(), next.y()))
                                .append('\n')
                                .append("currentX=")
                                .append(next.x())
                                .append("f;\ncurrentY=")
                                .append(next.y())
                                .append("f;\n");
                    }
                }
                case SVGPathBezierAbsolute ignored -> {
                    i++;
                    while (elements.get(i) instanceof SVGPathPoint) {
                        SVGPathPoint c1 = (SVGPathPoint) elements.get(i++);
                        SVGPathPoint c2 = (SVGPathPoint) elements.get(i++);
                        SVGPathPoint end = (SVGPathPoint) elements.get(i++);
                        sb.append(String.format(
                                        "sr.curve(currentX,currentY,%sf,%sf,%sf,%sf,%sf,%sf,50);",
                                        c1.x(), c1.y(), c2.x(), c2.y(), end.x(), end.y()))
                                .append('\n')
                                .append("currentX=")
                                .append(end.x())
                                .append("f;\ncurrentY=")
                                .append(end.y())
                                .append("f;\n");
                    }
                }
                case SVGPathBezierRelative ignored -> {
                    i++;
                    while (elements.get(i) instanceof SVGPathPoint) {
                        SVGPathPoint c1 = (SVGPathPoint) elements.get(i++);
                        SVGPathPoint c2 = (SVGPathPoint) elements.get(i++);
                        SVGPathPoint end = (SVGPathPoint) elements.get(i++);
                        sb.append(String.format(
                                        "sr.curve(currentX,currentY,currentX+%sf,currentY+%sf,currentX+%sf,currentY+%sf,currentX+%sf,currentY+%sf,50);",
                                        c1.x(), c1.y(), c2.x(), c2.y(), end.x(), end.y()))
                                .append('\n')
                                .append("currentX=")
                                .append(end.x())
                                .append("f;\ncurrentY=")
                                .append(end.y())
                                .append("f;\n");
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
