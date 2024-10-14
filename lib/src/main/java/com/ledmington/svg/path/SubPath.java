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
package com.ledmington.svg.path;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import com.ledmington.svg.Element;

/**
 * An SVG path element. Official documentation available <a
 * href="https://www.w3.org/TR/SVG2/paths.html#PathData">here</a>.
 */
public final class SubPath implements Element {

    // TODO: make modifiable by the user
    private static final int DEFAULT_CURVE_SEGMENTS = 50;

    private final List<PathElement> elements;

    /**
     * Creates a new SVGSubPath with the given list of path elements, which must not be empty.
     *
     * @param elements The non-empty list of path elements.
     */
    public SubPath(final List<PathElement> elements) {
        Objects.requireNonNull(elements);
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("Empty list of path elements");
        }
        if (!(elements.getFirst() instanceof MoveTo)) {
            throw new IllegalArgumentException(String.format(
                    "Expected first subpath element to be a 'moveto' element but was a '%s'",
                    elements.getFirst().toString()));
        }
        this.elements = Collections.unmodifiableList(elements);
    }

    /**
     * Returns the number of path elements contained in this subpath.
     *
     * @return The number of path elements.
     */
    public int getNumElements() {
        return elements.size();
    }

    /**
     * Returns the element at the given index.
     *
     * @param idx The index of the element.
     * @return The elment at the given index.
     */
    public PathElement getElement(final int idx) {
        return elements.get(idx);
    }

    public void draw(final ShapeRenderer sr, final Point initial) {

        Point current = null;
        for (final PathElement e : elements) {
            switch (e) {
                case MoveTo m -> {
                    if (m.isRelative()) {
                        current = (current == null) ? new Point(0.0, 0.0).add(initial) : current.add(initial);
                    } else {
                        current = initial;
                    }

                    for (int i = 0; i < m.getNumPoints(); i++) {
                        final Point next = m.getPoint(i);
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
                    for (int i = 0; i < b.getNumElements(); i++) {
                        final CubicBezierElement be = b.getElement(i);
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

    @Override
    public String toGDXShapeRenderer() {
        throw new Error("Not implemented");
    }

    @Override
    public String toString() {
        return "SubPath(elements=" + elements + ')';
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (!this.getClass().equals(other.getClass())) {
            return false;
        }
        final SubPath sp = (SubPath) other;
        return this.elements.equals(sp.elements);
    }

    @Override
    public int hashCode() {
        return 31 * 17 + elements.hashCode();
    }
}
