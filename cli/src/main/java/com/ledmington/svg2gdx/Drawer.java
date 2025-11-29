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
package com.ledmington.svg2gdx;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import com.ledmington.svg.Element;
import com.ledmington.svg.Image;
import com.ledmington.svg.Polyline;
import com.ledmington.svg.Rectangle;
import com.ledmington.svg.path.CubicBezier;
import com.ledmington.svg.path.CubicBezierElement;
import com.ledmington.svg.path.HorizontalLineTo;
import com.ledmington.svg.path.LineTo;
import com.ledmington.svg.path.MoveTo;
import com.ledmington.svg.path.Path;
import com.ledmington.svg.path.PathElement;
import com.ledmington.svg.path.Point;
import com.ledmington.svg.path.SubPath;
import com.ledmington.util.ParseUtils;

public final class Drawer {

    // TODO: make this modifiable by the user
    // Used also for arcs
    private static final int DEFAULT_CURVE_SEGMENTS = 50;

    private Drawer() {}

    /** Renders this image on the screen by using the given ShapeRenderer. */
    public static void draw(final ShapeRenderer sr, final Image image, final double viewportHeight) {
        Objects.requireNonNull(sr);
        Objects.requireNonNull(image);

        sr.setAutoShapeType(true);
        sr.begin();
        for (int i = 0; i < image.getNumElements(); i++) {
            final Element elem = image.getElement(i);
            Objects.requireNonNull(elem);
            switch (elem) {
                case Rectangle rect -> draw(sr, rect, viewportHeight);
                case Path path -> draw(sr, path, viewportHeight);
                case Polyline poly -> draw(sr, poly, viewportHeight);
                default -> throw new IllegalArgumentException(elem.toString());
            }
        }
        sr.end();
    }

    private static void draw(final ShapeRenderer sr, final Polyline poly, final double viewportHeight) {
        Objects.requireNonNull(sr);
        Objects.requireNonNull(poly);

        Point current = new Point(0.0, 0.0);
        for (int i = 0; i < poly.getNumPoints(); i++) {
            final Point p = poly.getPoint(i);
            sr.line((float) current.x(), (float) (viewportHeight - current.y()), (float) p.x(), (float) p.y());
            current = p;
        }
    }

    private static void draw(final ShapeRenderer sr, final Rectangle rect, final double viewportHeight) {
        Objects.requireNonNull(sr);
        Objects.requireNonNull(rect);

        sr.set(ShapeRenderer.ShapeType.Filled);
        sr.setColor(
                ParseUtils.byteToFloat(rect.fill().red()),
                ParseUtils.byteToFloat(rect.fill().green()),
                ParseUtils.byteToFloat(rect.fill().blue()),
                ParseUtils.byteToFloat(rect.fill().alpha()));
        sr.rect((float) rect.x(), (float) (viewportHeight - rect.height() - rect.y()), (float) rect.width(), (float)
                rect.height());

        sr.set(ShapeRenderer.ShapeType.Line);
        sr.setColor(
                ParseUtils.byteToFloat(rect.stroke().red()),
                ParseUtils.byteToFloat(rect.stroke().green()),
                ParseUtils.byteToFloat(rect.stroke().blue()),
                ParseUtils.byteToFloat(rect.stroke().alpha()));
        sr.rect((float) rect.x(), (float) (viewportHeight - rect.height() - rect.y()), (float) rect.width(), (float)
                rect.height());
    }

    private static void draw(final ShapeRenderer sr, final Path path, final double viewportHeight) {
        Objects.requireNonNull(sr);
        Objects.requireNonNull(path);

        drawFilledPath(sr, path, viewportHeight);
        drawOutlinePath(sr, path, viewportHeight);
    }

    private static void drawFilledPath(final ShapeRenderer sr, final Path path, final double viewportHeight) {
        sr.set(ShapeRenderer.ShapeType.Filled);
        sr.setColor(
                ParseUtils.byteToFloat(path.getFill().red()),
                ParseUtils.byteToFloat(path.getFill().green()),
                ParseUtils.byteToFloat(path.getFill().blue()),
                ParseUtils.byteToFloat(path.getFill().alpha()));

        // Gather points for the filled polygon
        for (int i = 0; i < path.getNumSubpaths(); i++) {
            final SubPath subpath = path.getSubpath(i);
            final List<Float> vertices = new ArrayList<>();

            final Point initial = ((MoveTo) subpath.getElement(0)).getPoint(0);
            Point current = new Point(0.0, 0.0);
            for (int j = 0; j < subpath.getNumElements(); j++) {
                final PathElement e = subpath.getElement(j);
                switch (e) {
                    case MoveTo m -> {
                        if (m.isRelative()) {
                            current = current.add(initial);
                        } else {
                            current = initial;
                        }

                        for (int k = 0; k < m.getNumPoints(); k++) {
                            final Point next = m.getPoint(k);
                            if (m.isRelative()) {
                                current = current.add(next);
                            } else {
                                current = next;
                            }
                            vertices.add((float) current.x());
                            vertices.add((float) (viewportHeight - current.y()));
                        }
                    }
                    case LineTo l -> {
                        for (int k = 0; k < l.getNumPoints(); k++) {
                            final Point p = l.getPoint(k);
                            if (l.isRelative()) {
                                current = current.add(p);
                            } else {
                                current = p;
                            }
                            vertices.add((float) current.x());
                            vertices.add((float) (viewportHeight - current.y()));
                        }
                    }
                    case HorizontalLineTo h -> {
                        for (int k = 0; k < h.getNumCoordinates(); k++) {
                            final double px = h.getCoordinate(k);
                            if (h.isRelative()) {
                                current = current.add(new Point(px, 0.0));
                            } else {
                                current = new Point(px, 0.0);
                            }
                            vertices.add((float) current.x());
                            vertices.add((float) (viewportHeight - current.y()));
                        }
                    }
                    case CubicBezier cb -> {
                        for (int k = 0; k < cb.getNumElements(); k++) {
                            final CubicBezierElement be = cb.getElement(k);

                            // Resolve control points & end point depending on relativeness
                            final Point p0 = current;
                            final Point p1 =
                                    cb.isRelative() ? current.add(be.firstControlPoint()) : be.firstControlPoint();
                            final Point p2 =
                                    cb.isRelative() ? current.add(be.secondControlPoint()) : be.secondControlPoint();
                            final Point p3 = cb.isRelative() ? current.add(be.endPoint()) : be.endPoint();

                            // Sample the cubic curve
                            for (int s = 1; s <= DEFAULT_CURVE_SEGMENTS; s++) {
                                final double t = (double) s / DEFAULT_CURVE_SEGMENTS;

                                final double mt = 1.0 - t;
                                final double x = mt * mt * mt * p0.x()
                                        + 3 * mt * mt * t * p1.x()
                                        + 3 * mt * t * t * p2.x()
                                        + t * t * t * p3.x();
                                final double y = mt * mt * mt * p0.y()
                                        + 3 * mt * mt * t * p1.y()
                                        + 3 * mt * t * t * p2.y()
                                        + t * t * t * p3.y();

                                vertices.add((float) x);
                                vertices.add((float) (viewportHeight - y));
                            }

                            current = p3; // Move current point to end of the curve
                        }
                    }

                    default -> throw new IllegalArgumentException(String.format("Unknown path element '%s'", e));
                }
            }

            if (vertices.size() < 6 /*|| (vertices.size() - 2) % 4 != 0*/) {
                throw new IllegalArgumentException(
                        String.format("Wrong number of triangle vertices: %,d", vertices.size()));
            }

            final float[] polygonVertices = convertToNative(vertices);
            // This mode of drawing is also called "triangle fan"
            for (int j = 2; j + 3 < polygonVertices.length; j += 4) {
                sr.triangle(
                        polygonVertices[0],
                        polygonVertices[1],
                        polygonVertices[j],
                        polygonVertices[j + 1],
                        polygonVertices[j + 2],
                        polygonVertices[j + 3]);
            }
        }
    }

    private static void drawOutlinePath(final ShapeRenderer sr, final Path path, final double viewportHeight) {
        sr.set(ShapeRenderer.ShapeType.Line);
        sr.setColor(
                ParseUtils.byteToFloat(path.getStroke().red()),
                ParseUtils.byteToFloat(path.getStroke().green()),
                ParseUtils.byteToFloat(path.getStroke().blue()),
                ParseUtils.byteToFloat(path.getStroke().alpha()));

        for (int i = 0; i < path.getNumSubpaths(); i++) {
            final SubPath subpath = path.getSubpath(i);
            draw(sr, subpath, ((MoveTo) subpath.getElement(0)).getPoint(0), viewportHeight);
        }
    }

    private static float[] convertToNative(final List<Float> v) {
        final float[] w = new float[v.size()];
        for (int i = 0; i < v.size(); i++) {
            w[i] = v.get(i);
        }
        return w;
    }

    private static void draw(
            final ShapeRenderer sr, final SubPath subpath, final Point initial, final double viewportHeight) {
        Objects.requireNonNull(sr);
        Objects.requireNonNull(subpath);
        Objects.requireNonNull(initial);

        Point current = new Point(0.0, 0.0);
        for (int i = 0; i < subpath.getNumElements(); i++) {
            final PathElement e = subpath.getElement(i);
            switch (e) {
                case MoveTo m -> {
                    if (m.isRelative()) {
                        current = current.add(initial);
                    } else {
                        current = initial;
                    }

                    for (int j = 0; j < m.getNumPoints(); j++) {
                        final Point next = m.getPoint(j);
                        if (m.isRelative()) {
                            sr.line(
                                    (float) current.x(),
                                    (float) (viewportHeight - current.y()),
                                    (float) (current.x() + next.x()),
                                    (float) (viewportHeight - current.y() + next.y()));
                            current = current.add(next);
                        } else {
                            sr.line(
                                    (float) current.x(),
                                    (float) (viewportHeight - current.y()),
                                    (float) next.x(),
                                    (float) (viewportHeight - next.y()));
                            current = next;
                        }
                    }
                }
                case LineTo l -> {
                    for (int j = 0; j < l.getNumPoints(); j++) {
                        final Point p = l.getPoint(j);
                        if (l.isRelative()) {
                            sr.line(
                                    (float) current.x(),
                                    (float) (viewportHeight - current.y()),
                                    (float) (current.x() + p.x()),
                                    (float) (viewportHeight - current.y() + p.y()));
                            current = current.add(p);
                        } else {
                            sr.line((float) current.x(), (float) (viewportHeight - current.y()), (float) p.x(), (float)
                                    (viewportHeight - p.y()));
                            current = p;
                        }
                    }
                }
                case CubicBezier cb -> {
                    for (int j = 0; j < cb.getNumElements(); j++) {
                        final CubicBezierElement be = cb.getElement(j);
                        if (cb.isRelative()) {
                            sr.curve(
                                    (float) current.x(),
                                    (float) (viewportHeight - current.y()),
                                    (float) (current.x()
                                            + be.firstControlPoint().x()),
                                    (float) (viewportHeight
                                            - current.y()
                                            + be.firstControlPoint().y()),
                                    (float) (current.x()
                                            + be.secondControlPoint().x()),
                                    (float) (viewportHeight
                                            - current.y()
                                            + be.secondControlPoint().y()),
                                    (float) (current.x() + be.endPoint().x()),
                                    (float) (current.y() + be.endPoint().y()),
                                    DEFAULT_CURVE_SEGMENTS);
                            current = current.add(be.endPoint());
                        } else {
                            sr.curve(
                                    (float) current.x(),
                                    (float) (viewportHeight - current.y()),
                                    (float) be.firstControlPoint().x(),
                                    (float) (viewportHeight
                                            - be.firstControlPoint().y()),
                                    (float) be.secondControlPoint().x(),
                                    (float) (viewportHeight
                                            - be.secondControlPoint().y()),
                                    (float) be.endPoint().x(),
                                    (float) (viewportHeight - be.endPoint().y()),
                                    DEFAULT_CURVE_SEGMENTS);
                            current = be.endPoint();
                        }
                    }
                }
                default -> throw new IllegalArgumentException(String.format("Unknown SVG path element type '%s'", e));
            }
        }

        // close the path
        sr.line((float) current.x(), (float) (viewportHeight - current.y()), (float) initial.x(), (float)
                (viewportHeight - initial.y()));
    }
}
