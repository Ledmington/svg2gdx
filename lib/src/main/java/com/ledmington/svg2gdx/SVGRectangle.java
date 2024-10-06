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

import java.util.Objects;

public final class SVGRectangle implements SVGElement {

    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final boolean filled;
    private final String colorName;

    public SVGRectangle(
            final double x,
            final double y,
            final double width,
            final double height,
            final boolean filled,
            final String colorName) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.filled = filled;
        this.colorName = Objects.requireNonNull(colorName);

        if (width <= 0.0 || height <= 0.0) {
            throw new IllegalArgumentException(
                    String.format("Invalid width and height arguments: %f %f", width, height));
        }
    }

    @Override
    public String toGDXShapeRenderer() {
        return "sr.set(" + (filled ? "ShapeType.Filled" : "ShapeType.Line") + ");\n"
                + String.format("sr.setColor(%s);", colorName) + '\n'
                + String.format("sr.rect(%sf,%sf,%sf,%sf);", x, y, width, height) + '\n';
    }
}
