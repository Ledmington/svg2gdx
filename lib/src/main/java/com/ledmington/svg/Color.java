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

/**
 * An RGBA color.
 *
 * @param red The red component.
 * @param green The green component.
 * @param blue The blue component.
 * @param alpha The alpha component.
 */
public record Color(byte red, byte green, byte blue, byte alpha) implements Element {

    /** Creates a default RGBA color with all components set to 0 (transparent black). */
    public Color() {
        this((byte) 0, (byte) 0, (byte) 0, (byte) 0);
    }

    @Override
    public String toString() {
        return String.format("SVGColor[red=0x%02x, green=0x%02x, blue=0x%02x, aalpha=0x%02x]", red, green, blue, alpha);
    }
}
