/*
 * svg2gdx - A converter from SVG to LibGDX ShapeRenderer code.
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

public final class SVGColor implements SVGElement, Comparable<SVGColor> {

    private final int x;

    public SVGColor(final byte r, final byte g, final byte b, final byte a) {
        this.x = (asInt(r) << 24) | (asInt(g) << 16) | (asInt(b) << 8) | (asInt(a));
    }

    public SVGColor() {
        this.x = 0x00000000;
    }

    private int asInt(final byte b) {
        return ((int) b) & 0x000000ff;
    }

    private double asDouble(final int i) {
        return ((double) (i)) / 255.0;
    }

    @Override
    public String toGDXShapeRenderer() {
        return String.format(
                "new Color(%sf,%sf,%sf,%sf); // #%08X",
                asDouble((x >>> 24) & 0x000000ff),
                asDouble((x >>> 16) & 0x000000ff),
                asDouble((x >>> 8) & 0x000000ff),
                asDouble(x & 0x000000ff),
                x);
    }

    @Override
    public int compareTo(final SVGColor other) {
        return Integer.compare(this.x, other.x);
    }

    public String toString() {
        return String.format("#%08X", x);
    }

    public int hashCode() {
        return x;
    }

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
        return this.x == ((SVGColor) other).x;
    }
}
