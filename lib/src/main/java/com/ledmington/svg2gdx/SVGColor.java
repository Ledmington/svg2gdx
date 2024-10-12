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

/**
 * An RGBA color.
 *
 * @param r The red component.
 * @param g The green component.
 * @param b The blue component.
 * @param a The alpha component.
 */
public record SVGColor(byte r, byte g, byte b, byte a) implements SVGElement {

    /** Creates a default RGBA color with all components set to 0 (black). */
    public SVGColor() {
        this((byte) 0, (byte) 0, (byte) 0, (byte) 0);
    }

    @Override
    public String toGDXShapeRenderer() {
        return String.format(
                "new Color(%sf, %sf, %sf, %sf); // #%02X%02X%02X%02X",
                ParseUtils.byteToFloat(r),
                ParseUtils.byteToFloat(g),
                ParseUtils.byteToFloat(b),
                ParseUtils.byteToFloat(a),
                r,
                g,
                b,
                a);
    }

    @Override
    public String toString() {
        return String.format("SVGColor[r=0x%02x, g=0x%02x, b=0x%02x, a=0x%02x]", r, g, b, a);
    }
}
