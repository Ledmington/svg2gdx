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

import java.util.List;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import com.ledmington.svg2gdx.ParseUtils;
import com.ledmington.svg2gdx.SVGColor;
import com.ledmington.svg2gdx.SVGElement;
import com.ledmington.svg2gdx.SVGPalette;

/**
 * An SVG path element. Official documentation available <a
 * href="https://www.w3.org/TR/SVG2/paths.html#PathData">here</a>.
 */
public record SVGSubPath(List<SVGPathElement> elements) implements SVGElement {

    private static final int DEFAULT_CURVE_SEGMENTS = 50;

    public void draw(
            final ShapeRenderer sr, final SVGPalette palette, final String colorName, final SVGPathPoint initial) {
        final SVGColor c = palette.getFromName(colorName);
        sr.setColor(
                ParseUtils.byteToFloat(c.r()),
                ParseUtils.byteToFloat(c.g()),
                ParseUtils.byteToFloat(c.b()),
                ParseUtils.byteToFloat(c.a()));

        SVGPathPoint current = null;
        for (final SVGPathElement e : elements) {
            switch (e) {
                case SVGPathMoveto m -> {
                    if (m.isRelative()) {
                        current = (current == null) ? new SVGPathPoint(0.0, 0.0).add(initial) : current.add(initial);
                    } else {
                        current = initial;
                    }

                    for (final SVGPathPoint next : m.implicitLines()) {
                        if (m.isRelative()) {
                            sr.line((float) current.x(), (float) current.y(), (float) (current.x() + next.x()), (float)
                                    (current.y() + next.y()));
                            current = current.add(next);
                        } else {
                            sr.line((float) current.x(), (float) current.y(), (float) next.x(), (float) next.y());
                            current = next;
                        }
                    }
                }
                case SVGPathLineto l -> {
                    for (final SVGPathPoint p : l.points()) {
                        if (l.isRelative()) {
                            sr.line((float) current.x(), (float) current.y(), (float) (current.x() + p.x()), (float)
                                    (current.y() + p.y()));
                            current = current.add(p);
                        } else {
                            sr.line((float) current.x(), (float) current.y(), (float) p.x(), (float) p.y());
                            current = p;
                        }
                    }
                }
                case SVGPathBezier b -> {
                    for (final SVGPathBezierElement be : b.elements()) {
                        if (b.isRelative()) {
                            sr.curve(
                                    (float) current.x(),
                                    (float) current.y(),
                                    (float) (current.x()
                                            + be.firstControlPoint().x()),
                                    (float) (current.y()
                                            + be.firstControlPoint().y()),
                                    (float) (current.x()
                                            + be.secondControlPoint().x()),
                                    (float) (current.y()
                                            + be.secondControlPoint().y()),
                                    (float) (current.x() + be.endPoint().x()),
                                    (float) (current.y() + be.endPoint().y()),
                                    DEFAULT_CURVE_SEGMENTS);
                            current = current.add(be.endPoint());
                        } else {
                            sr.curve(
                                    (float) current.x(),
                                    (float) current.y(),
                                    (float) be.firstControlPoint().x(),
                                    (float) be.firstControlPoint().y(),
                                    (float) be.secondControlPoint().x(),
                                    (float) be.secondControlPoint().y(),
                                    (float) be.endPoint().x(),
                                    (float) be.endPoint().y(),
                                    DEFAULT_CURVE_SEGMENTS);
                            current = be.endPoint();
                        }
                    }
                }
                default -> throw new IllegalArgumentException(String.format("Unknown SVG path element type '%s'", e));
            }
        }
    }

    @Override
    public String toGDXShapeRenderer() {
        final StringBuilder sb = new StringBuilder();
        sb.append("sr.setColor(Color.BLACK);\n");

        SVGPathPoint initialPoint = null;
        int i = 0;
        while (i < elements.size()) {
            final SVGPathElement elem = elements.get(i);
            switch (elem) {
                case SVGPathMoveto m -> {
                    i++;
                    final SVGPathPoint current = (SVGPathPoint) elements.get(i++);
                    sb.append("currentX = ")
                            .append(current.x())
                            .append("f;\ncurrentY = ")
                            .append(current.y())
                            .append("f;\n");
                    if (initialPoint == null) {
                        initialPoint = current;
                        sb.append("initialX = ")
                                .append(current.x())
                                .append("f;\ninitialY = ")
                                .append(current.y())
                                .append("f;\n");
                    }
                    if (m.isRelative()) {
                        while (elements.get(i) instanceof SVGPathPoint) {
                            final SVGPathPoint next = (SVGPathPoint) elements.get(i++);
                            sb.append(String.format(
                                            "sr.line(currentX, currentY, currentX + %sf, currentY + %sf);",
                                            next.x(), next.y()))
                                    .append('\n')
                                    .append("currentX = ")
                                    .append(next.x())
                                    .append("f;\ncurrentY = ")
                                    .append(next.y())
                                    .append("f;\n");
                        }
                    } else {
                        while (elements.get(i) instanceof SVGPathPoint) {
                            final SVGPathPoint next = (SVGPathPoint) elements.get(i++);
                            sb.append(String.format("sr.line(currentX, currentY, %sf, %sf);", next.x(), next.y()))
                                    .append('\n')
                                    .append("currentX = ")
                                    .append(next.x())
                                    .append("f;\ncurrentY = ")
                                    .append(next.y())
                                    .append("f;\n");
                        }
                    }
                }
                case SVGPathLineto l -> {
                    sb.append("currentX = ")
                            .append(l.points().getFirst().x())
                            .append("f;\ncurrentY = ")
                            .append(l.points().getFirst().y())
                            .append("f;\n");
                    if (l.isRelative()) {
                        for (final SVGPathPoint next : l.points()) {
                            sb.append(String.format(
                                            "sr.line(currentX, currentY, currentX + %sf, currentY + %sf);",
                                            next.x(), next.y()))
                                    .append('\n')
                                    .append("currentX = ")
                                    .append(next.x())
                                    .append("f;\ncurrentY = ")
                                    .append(next.y())
                                    .append("f;\n");
                        }
                    } else {
                        for (final SVGPathPoint next : l.points()) {
                            sb.append(String.format("sr.line(currentX, currentY, %sf, %sf);", next.x(), next.y()))
                                    .append('\n')
                                    .append("currentX = ")
                                    .append(next.x())
                                    .append("f;\ncurrentY = ")
                                    .append(next.y())
                                    .append("f;\n");
                        }
                    }
                }
                case SVGPathBezier b -> {
                    if (b.isRelative()) {
                        while (elements.get(i) instanceof SVGPathPoint) {
                            SVGPathPoint c1 = (SVGPathPoint) elements.get(i++);
                            SVGPathPoint c2 = (SVGPathPoint) elements.get(i++);
                            SVGPathPoint end = (SVGPathPoint) elements.get(i++);
                            sb.append(String.format(
                                            "sr.curve(currentX, currentY, currentX + %sf, currentY + %sf, currentX + %sf, currentY + %sf, currentX + %sf, currentY + %sf, %d);",
                                            c1.x(), c1.y(), c2.x(), c2.y(), end.x(), end.y(), DEFAULT_CURVE_SEGMENTS))
                                    .append('\n')
                                    .append("currentX = ")
                                    .append(end.x())
                                    .append("f;\ncurrentY = ")
                                    .append(end.y())
                                    .append("f;\n");
                        }
                    } else {
                        while (elements.get(i) instanceof SVGPathPoint) {
                            SVGPathPoint c1 = (SVGPathPoint) elements.get(i++);
                            SVGPathPoint c2 = (SVGPathPoint) elements.get(i++);
                            SVGPathPoint end = (SVGPathPoint) elements.get(i++);
                            sb.append(String.format(
                                            "sr.curve(currentX, currentY, %sf, %sf, %sf, %sf, %sf, %sf, %d);",
                                            c1.x(), c1.y(), c2.x(), c2.y(), end.x(), end.y(), DEFAULT_CURVE_SEGMENTS))
                                    .append('\n')
                                    .append("currentX = ")
                                    .append(end.x())
                                    .append("f;\ncurrentY = ")
                                    .append(end.y())
                                    .append("f;\n");
                        }
                    }
                }
                case SVGPathClosepath ignored -> {
                    sb.append("sr.line(currentX, currentY, initialX, initialY);\n");
                    sb.append("initialX = 0.0f;\ninitialY = 0.0f;\n");
                    initialPoint = null;
                    sb.append("currentX = 0.0f;\ncurrentY = 0.0f;\n");
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
