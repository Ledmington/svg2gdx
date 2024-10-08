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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import com.ledmington.svg2gdx.path.SVGPath;

/** A parsed SVG image. Official reference available <a href="https://www.w3.org/TR/SVG2/">here</a>. */
public final class SVGImage implements SVGElement {

    private final double width;
    private final double height;
    private final SVGPalette palette;
    private final List<SVGElement> elements;

    public SVGImage(
            final double width, final double height, final SVGPalette palette, final List<SVGElement> elements) {
        this.width = width;
        this.height = height;
        this.palette = Objects.requireNonNull(palette);
        this.elements = new ArrayList<>(Objects.requireNonNull(elements));
    }

    public void draw(final ShapeRenderer sr) {
        sr.setAutoShapeType(true);
        sr.begin();
        for (final SVGElement elem : elements) {
            switch (elem) {
                case SVGRectangle rect -> rect.draw(sr, palette);
                case SVGPath path -> path.draw(sr, palette);
                default -> throw new Error(elem.toString());
            }
        }
        sr.end();
    }

    @Override
    public String toGDXShapeRenderer() {
        final StringBuilder sb = new StringBuilder();
        sb.append("private void draw(final float x, final float y) {\n")
                .append(String.format("final double width = %s;", width))
                .append('\n')
                .append(String.format("final double height = %s;", height))
                .append('\n')
                .append(palette.toGDXShapeRenderer())
                .append("final ShapeRenderer sr = @Place here your ShapeRenderer@;\n")
                .append("float currentX = 0.0f;\n")
                .append("float currentY = 0.0f;\n")
                .append("float initialX = 0.0f;\n")
                .append("float initialY = 0.0f;\n")
                .append("sr.setAutoShapeType(true);\n")
                .append("sr.begin();\n");
        for (final SVGElement elem : elements) {
            sb.append(elem.toGDXShapeRenderer());
        }
        sb.append("sr.end();\n}\n");
        return sb.toString();
    }
}
