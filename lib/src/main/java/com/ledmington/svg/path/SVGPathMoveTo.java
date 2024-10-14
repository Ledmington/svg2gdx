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

/**
 * The SVG path element relative to 'moveto' commands. Official documentation available <a
 * href="https://www.w3.org/TR/SVG2/paths.html#PathDataMovetoCommands">here</a>.
 */
public final class SVGPathMoveTo implements SVGPathElement {

    private final boolean isRelative;
    private final List<SVGPathPoint> points;

    /**
     * Creates a new path 'moveto' element.
     *
     * @param isRelative True if this element is relative, false if it is absolute.
     * @param points The non-empty list of points of this element.
     */
    public SVGPathMoveTo(final boolean isRelative, final List<SVGPathPoint> points) {
        this.isRelative = isRelative;
        Objects.requireNonNull(points);
        if (points.isEmpty()) {
            throw new IllegalArgumentException("Empty list of points");
        }
        this.points = Collections.unmodifiableList(points);
    }

    /**
     * Returns true if this element is relative, false if it is absolute.
     *
     * @return True if relative, false if absolute.
     */
    public boolean isRelative() {
        return isRelative;
    }

    /**
     * Returns the number of points in this element.
     *
     * @return The number of points in this element.
     */
    public int getNumPoints() {
        return points.size();
    }

    /**
     * Returns the point at the given index.
     *
     * @param idx The index of the element.
     * @return The element at the given index.
     */
    public SVGPathPoint getPoint(final int idx) {
        return points.get(idx);
    }

    @Override
    public String toString() {
        return "SVGPathMoveTo(isRelative=" + isRelative + ";points=" + points + ")";
    }
}
