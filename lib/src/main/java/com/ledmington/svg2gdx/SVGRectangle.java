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

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * A rectangle aligned with the axis. Official documentation available <a
 * href="https://www.w3.org/TR/SVG2/shapes.html#RectElement">here</a>.
 *
 * @param x x coordinate of the left side of the rectangle.
 * @param y y coordinate of the top side of the rectangle.
 * @param width The width of the rectangle.
 * @param height The height of the rectangle.
 * @param filled Whether this rectangle is filled with color or not. If not filled, its color applies only to the
 *     borders.
 * @param colorName The name of the color of this rectangle.
 */
record SVGRectangle(double x, double y, double width, double height, boolean filled, String colorName)
        implements SVGElement {
    public SVGRectangle(
            final double x,
            final double y,
            final double width,
            final double height,
            final boolean filled,
            final String colorName) {
        if (width <= 0.0 || height <= 0.0) {
            throw new IllegalArgumentException(
                    String.format("Invalid width and height arguments: %f %f", width, height));
        }

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.filled = filled;
        this.colorName = Objects.requireNonNull(colorName);
    }

    public void draw(final ShapeRenderer sr, final SVGPalette palette) {
        sr.set(filled ? ShapeRenderer.ShapeType.Filled : ShapeRenderer.ShapeType.Line);
        final SVGColor c = palette.getFromName(colorName);
        sr.setColor(
                ((float) c.r()) / 255.0f, ((float) c.g()) / 255.0f, ((float) c.b()) / 255.0f, ((float) c.a()) / 255.0f);
        sr.rect((float) x, (float) y, (float) width, (float) height);
    }

    @Override
    public String toGDXShapeRenderer() {
        return "sr.set(" + (filled ? "ShapeType.Filled" : "ShapeType.Line") + ");\n"
                + String.format("sr.setColor(%s);", colorName) + '\n'
                + String.format("sr.rect(%sf,%sf,%sf,%sf);", x, y, width, height) + '\n';
    }
}
