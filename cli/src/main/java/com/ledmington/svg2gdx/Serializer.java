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

import com.ledmington.svg.Color;
import com.ledmington.svg.Element;
import com.ledmington.svg.Image;
import com.ledmington.util.ParseUtils;

public final class Serializer {

    private Serializer() {}

    public static String serialize(final Image image) {
        Objects.requireNonNull(image);

        final StringBuilder sb = new StringBuilder();
        serialize(sb, image);
        return sb.toString();
    }

    private static void serialize(final StringBuilder sb, final Image image) {
        sb.append("private void draw(final float x, final float y) {\n")
                .append(String.format(
                        "final double width = %s;", image.getViewBox().width()))
                .append('\n')
                .append(String.format(
                        "final double height = %s;", image.getViewBox().width()))
                .append('\n')
                .append("final ShapeRenderer sr = @Place here your ShapeRenderer@;\n")
                .append("float currentX = 0.0f;\n")
                .append("float currentY = 0.0f;\n")
                .append("float initialX = 0.0f;\n")
                .append("float initialY = 0.0f;\n")
                .append("sr.setAutoShapeType(true);\n")
                .append("sr.begin();\n");
        for (int i = 0; i < image.getNumElements(); i++) {
            final Element elem = image.getElement(i);
            switch (elem) {
                case Color c -> serialize(sb, c);
                default -> throw new IllegalArgumentException(String.format("Unknown SVG element '%s'", elem));
            }
        }
        sb.append("sr.end();\n}\n");
    }

    private static void serialize(final StringBuilder sb, final Color c) {
        sb.append(String.format(
                "new Color(%sf, %sf, %sf, %sf); // #%02X%02X%02X%02X",
                ParseUtils.byteToFloat(c.red()),
                ParseUtils.byteToFloat(c.green()),
                ParseUtils.byteToFloat(c.blue()),
                ParseUtils.byteToFloat(c.alpha()),
                c.red(),
                c.green(),
                c.blue(),
                c.alpha()));
    }
}
