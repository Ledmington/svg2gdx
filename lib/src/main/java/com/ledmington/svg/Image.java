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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import com.ledmington.svg.path.Path;
import com.ledmington.util.HashUtils;

/** A parsed SVG image. Official reference available <a href="https://www.w3.org/TR/SVG2/">here</a>. */
public final class Image implements Element {

    private final ViewBox viewBox;
    private final double width;
    private final double height;
    private final List<Element> elements;

    /**
     * Creates a new SVGImage with the given data.
     *
     * @param width The width of the image.
     * @param height The height of the image.
     * @param elements The inner elements of this image.
     */
    public Image(final ViewBox viewBox, final double width, final double height, final List<Element> elements) {
        this.viewBox = Objects.requireNonNull(viewBox);
        if (width < 0.0 || height < 0.0) {
            throw new IllegalArgumentException(String.format("Invalid width and height: %f x %f", width, height));
        }
        this.width = width;
        this.height = height;
        this.elements = Collections.unmodifiableList(Objects.requireNonNull(elements));
    }

    /**
     * Renders this image on the screen by using the given ShapeRenderer.
     *
     * @param sr The ShapeRenderer to be used.
     */
    public void draw(final ShapeRenderer sr) {
        sr.setAutoShapeType(true);
        sr.begin();
        for (final Element elem : elements) {
            switch (elem) {
                case Rectangle rect -> rect.draw(sr);
                case Path path -> path.draw(sr);
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
                .append("final ShapeRenderer sr = @Place here your ShapeRenderer@;\n")
                .append("float currentX = 0.0f;\n")
                .append("float currentY = 0.0f;\n")
                .append("float initialX = 0.0f;\n")
                .append("float initialY = 0.0f;\n")
                .append("sr.setAutoShapeType(true);\n")
                .append("sr.begin();\n");
        for (final Element elem : elements) {
            sb.append(elem.toGDXShapeRenderer());
        }
        sb.append("sr.end();\n}\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Image(viewBox=" + viewBox + ";width=" + width + ";height=" + height + ";elements=" + elements + ")";
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + viewBox.hashCode();
        h = 31 * h + HashUtils.hash(width);
        h = 31 * h + HashUtils.hash(height);
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
        final Image img = (Image) other;
        return this.viewBox.equals(img.viewBox)
                && this.width == img.width
                && this.height == img.height
                && this.elements.equals(img.elements);
    }
}
