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

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * A rectangle aligned with the axis. Official documentation available <a
 * href="https://www.w3.org/TR/SVG2/shapes.html#RectElement">here</a>.
 *
 * @param x x coordinate of the left side of the rectangle.
 * @param y y coordinate of the top side of the rectangle.
 * @param width The width of the rectangle.
 * @param height The height of the rectangle.
 */
record SVGRectangle(double x, double y, double width, double height, SVGColor fill, SVGColor stroke, double strokeWidth)
        implements SVGElement {
    public void draw(final ShapeRenderer sr, final SVGPalette palette) {
        sr.set(ShapeRenderer.ShapeType.Filled);
        sr.setColor(
                ParseUtils.byteToFloat(fill.r()),
                ParseUtils.byteToFloat(fill.g()),
                ParseUtils.byteToFloat(fill.b()),
                ParseUtils.byteToFloat(fill.a()));
        sr.rect((float) x, (float) y, (float) width, (float) height);

        sr.set(ShapeRenderer.ShapeType.Line);
        sr.setColor(
                ParseUtils.byteToFloat(stroke.r()),
                ParseUtils.byteToFloat(stroke.g()),
                ParseUtils.byteToFloat(stroke.b()),
                ParseUtils.byteToFloat(stroke.a()));
        sr.rect((float) x, (float) y, (float) width, (float) height);
    }

    @Override
    public String toGDXShapeRenderer() {
        // return "sr.set(" + (filled ? "ShapeType.Filled" : "ShapeType.Line") + ");\n"
        // + String.format("sr.setColor(%s);", colorName) + '\n'
        // + String.format("sr.rect(%sf, %sf, %sf, %sf);", x, y, width, height) + '\n';
        throw new Error("Not implemented");
    }
}
