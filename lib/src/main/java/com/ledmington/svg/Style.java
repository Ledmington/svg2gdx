/*
 * svg2gdx - A converter from SVG to libGDX ShapeRenderer code.
 * Copyright (C) 2023-2025 Filippo Barbari <filippo.barbari@gmail.com>
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

public record Style(Color fill, Color stroke, double strokeWidth) {

    public Style(final Color fill, final Color stroke, final double strokeWidth) {
        this.fill = Objects.requireNonNull(fill);
        this.stroke = Objects.requireNonNull(stroke);

        if (strokeWidth <= 0.0) {
            throw new IllegalArgumentException(String.format("Invalid stroke-width: %f", strokeWidth));
        }
        this.strokeWidth = strokeWidth;
    }

    public Style() {
        this(new Color(), new Color(), 1.0);
    }
}
