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

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import com.ledmington.svg.Element;
import com.ledmington.svg.Image;
import com.ledmington.svg.Rectangle;
import com.ledmington.svg.path.CubicBezier;
import com.ledmington.svg.path.CubicBezierElement;
import com.ledmington.svg.path.LineTo;
import com.ledmington.svg.path.MoveTo;
import com.ledmington.svg.path.Path;
import com.ledmington.svg.path.PathElement;
import com.ledmington.svg.path.Point;
import com.ledmington.svg.path.SubPath;
import com.ledmington.util.ParseUtils;

public final class Drawer {

    // TODO: make modifiable by the user
    private static final int DEFAULT_CURVE_SEGMENTS = 50;

    private Drawer() {}

    /**
     * Renders this image on the screen by using the given ShapeRenderer.
     *
     * @param sr The ShapeRenderer to be used.
     */
    public static void draw(final ShapeRenderer sr, final Image image) {
        sr.setAutoShapeType(true);
        sr.begin();
        for (int i = 0; i < image.getNumElements(); i++) {
            final Element elem = image.getElement(i);
            switch (elem) {
                case Rectangle rect -> draw(sr, rect);
                case Path path -> draw(sr, path);
                default -> throw new IllegalArgumentException(elem.toString());
            }
        }
        sr.end();
    }

    public static void draw(final ShapeRenderer sr, final Rectangle rect) {
        sr.set(ShapeRenderer.ShapeType.Filled);
        sr.setColor(
                ParseUtils.byteToFloat(rect.fill().r()),
                ParseUtils.byteToFloat(rect.fill().g()),
                ParseUtils.byteToFloat(rect.fill().b()),
                ParseUtils.byteToFloat(rect.fill().a()));
        sr.rect((float) rect.x(), (float) rect.y(), (float) rect.width(), (float) rect.height());

        sr.set(ShapeRenderer.ShapeType.Line);
        sr.setColor(
                ParseUtils.byteToFloat(rect.stroke().r()),
                ParseUtils.byteToFloat(rect.stroke().g()),
                ParseUtils.byteToFloat(rect.stroke().b()),
                ParseUtils.byteToFloat(rect.stroke().a()));
        sr.rect((float) rect.x(), (float) rect.y(), (float) rect.width(), (float) rect.height());
    }

    public static void draw(final ShapeRenderer sr, final Path path) {
        for (int i = 0; i < path.getNumSubpaths(); i++) {
            final SubPath subpath = path.getSubpath(i);
            draw(sr, subpath, ((MoveTo) subpath.getElement(0)).getPoint(0));
        }
    }

    public static void draw(final ShapeRenderer sr, final SubPath subpath, final Point initial) {
        Point current = null;
        for (int i = 0; i < subpath.getNumElements(); i++) {
            final PathElement e = subpath.getElement(i);
            switch (e) {
                case MoveTo m -> {
                    if (m.isRelative()) {
                        current = (current == null) ? new Point(0.0, 0.0).add(initial) : current.add(initial);
                    } else {
                        current = initial;
                    }

                    for (int j = 0; j < m.getNumPoints(); j++) {
                        final Point next = m.getPoint(j);
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
                case LineTo l -> {
                    for (int j = 0; j < l.getNumPoints(); j++) {
                        final Point p = l.getPoint(j);
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
                case CubicBezier b -> {
                    for (int j = 0; j < b.getNumElements(); j++) {
                        final CubicBezierElement be = b.getElement(j);
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

        // close the path
        sr.line((float) current.x(), (float) current.y(), (float) initial.x(), (float) initial.y());
    }
}
