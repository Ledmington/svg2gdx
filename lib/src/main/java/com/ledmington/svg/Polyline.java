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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.ledmington.svg.path.Point;

/**
 * An SVG 'polyline' element. Practically equivalent to a path with only absolute 'moveto' and 'lineto' commands.
 * Official Documentation available <a href="https://www.w3.org/TR/SVG2/shapes.html#PolylineElement">here</a>.
 */
public final class Polyline implements Element {

    private final List<Point> points;

    public Polyline(final List<Point> points) {
        Objects.requireNonNull(points);
        if (points.isEmpty()) {
            throw new IllegalArgumentException("Empty list of points");
        }
        this.points = Collections.unmodifiableList(points);
    }

    @Override
    public String toGDXShapeRenderer() {
        throw new Error("Not implemented");
    }

    @Override
    public String toString() {
        return "Polyline(points=" + points + ")";
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + points.hashCode();
        return h;
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
        final Polyline pl = (Polyline) other;
        return this.points.equals(pl.points);
    }
}
