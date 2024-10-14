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

import com.ledmington.util.HashUtils;

/**
 * An SVG cubic Bezier curve (with implicit control point) command ("smooth curveto"). Official documentation available
 * <a href="https://www.w3.org/TR/SVG2/paths.html#PathDataCubicBezierCommands">here</a>.
 */
public final class SmoothBezier implements PathElement {

    private final boolean isRelative;
    private final List<SmoothBezierElement> elements;

    public SmoothBezier(final boolean isRelative, final List<SmoothBezierElement> elements) {
        this.isRelative = isRelative;
        Objects.requireNonNull(elements);
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("Empty list of bezier elements");
        }
        this.elements = Collections.unmodifiableList(elements);
    }

    public boolean isRelative() {
        return isRelative;
    }

    public int getNumElements() {
        return elements.size();
    }

    public SmoothBezierElement getElement(final int idx) {
        return elements.get(idx);
    }

    @Override
    public String toString() {
        return "SmoothBezier(isRelative=" + isRelative + ";elements=" + elements + ")";
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + HashUtils.hash(isRelative);
        h = 31 * h + elements.hashCode();
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
        final SmoothBezier sb = (SmoothBezier) other;
        return this.isRelative == sb.isRelative && this.elements.equals(sb.elements);
    }
}
