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

import com.ledmington.util.HashUtils;

/**
 * An SVG "horizontal lineto" command. Official documentation available <a
 * href="https://www.w3.org/TR/SVG2/paths.html#PathDataLinetoCommands">here</a>.
 */
public final class HorizontalLineTo implements PathElement {

    private final boolean isRelative;
    private final List<Double> x;

    public HorizontalLineTo(final boolean isRelative, final List<Double> x) {
        this.isRelative = isRelative;
        Objects.requireNonNull(x);
        if (x.isEmpty()) {
            throw new IllegalArgumentException("Empty list of coordinates");
        }
        this.x = Collections.unmodifiableList(x);
    }

    public boolean isRelative() {
        return isRelative;
    }

    public int getNumCoordinates() {
        return x.size();
    }

    public double getCoordinate(final int idx) {
        return x.get(idx);
    }

    @Override
    public String toString() {
        return "HorizontalLineTo(isRelative=" + isRelative + ";coordinates=" + x + ")";
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + HashUtils.hash(isRelative);
        h = 31 * h + x.hashCode();
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
        final HorizontalLineTo hlt = (HorizontalLineTo) other;
        return this.isRelative == hlt.isRelative && this.x.equals(hlt.x);
    }
}
