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

import java.util.Objects;

/**
 * A rectangle aligned with the axis. Official documentation available <a
 * href="https://www.w3.org/TR/SVG2/shapes.html#RectElement">here</a>.
 *
 * @param x x coordinate of the left side of the rectangle.
 * @param y y coordinate of the top side of the rectangle.
 * @param width The width of the rectangle.
 * @param height The height of the rectangle.
 */
public record Rectangle(double x, double y, double width, double height, Color fill, Color stroke, double strokeWidth)
        implements Element {
    public Rectangle(
            final double x,
            final double y,
            final double width,
            final double height,
            final Color fill,
            final Color stroke,
            final double strokeWidth) {
        this.x = x;
        this.y = y;

        if (width <= 0.0 || height <= 0.0) {
            throw new IllegalArgumentException(String.format("Invalid width and height: %f x %f", width, height));
        }
        this.width = width;
        this.height = height;

        this.fill = Objects.requireNonNull(fill);
        this.stroke = Objects.requireNonNull(stroke);

        if (strokeWidth <= 0.0) {
            throw new IllegalArgumentException(String.format("Invalid stroke-width: %f", strokeWidth));
        }
        this.strokeWidth = strokeWidth;
    }
}
