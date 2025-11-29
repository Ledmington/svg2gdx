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
package com.ledmington.svg.path;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.ledmington.svg.Color;
import com.ledmington.svg.Element;
import com.ledmington.util.HashUtils;

public final class Path implements Element {

    private final List<SubPath> subpaths;
    private final Color fill;
    private final Color stroke;
    private final double strokeWidth;

    public Path(final List<SubPath> subpaths, final Color fill, final Color stroke, final double strokeWidth) {
        Objects.requireNonNull(subpaths);
        if (subpaths.isEmpty()) {
            throw new IllegalArgumentException("Empty list of subpaths");
        }
        this.subpaths = Collections.unmodifiableList(subpaths);
        this.fill = Objects.requireNonNull(fill);
        this.stroke = Objects.requireNonNull(stroke);
        this.strokeWidth = strokeWidth;
    }

    public int getNumSubpaths() {
        return subpaths.size();
    }

    public SubPath getSubpath(final int idx) {
        return subpaths.get(idx);
    }

    public Color getFill() {
        return fill;
    }

    public Color getStroke() {
        return stroke;
    }

    @Override
    public String toString() {
        return "Path(subpaths=" + subpaths + ";fill=" + fill + ";stroke=" + stroke + ";strokeWidth=" + strokeWidth
                + ")";
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + subpaths.hashCode();
        h = 31 * h + fill.hashCode();
        h = 31 * h + stroke.hashCode();
        h = 31 * h + HashUtils.hash(strokeWidth);
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
        if (!(other instanceof Path p)) {
            return false;
        }
        return this.subpaths.equals(p.subpaths)
                && this.fill.equals(p.fill)
                && this.stroke.equals(p.stroke)
                && this.strokeWidth == p.strokeWidth;
    }
}
