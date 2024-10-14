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
package com.ledmington.svg.path;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Arc implements PathElement {

    private final boolean isRelative;
    private final List<ArcElement> elements;

    public Arc(final boolean isRelative, final List<ArcElement> elements) {
        this.isRelative = isRelative;
        Objects.requireNonNull(elements);
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("Empty list of elements");
        }
        this.elements = Collections.unmodifiableList(elements);
    }

    @Override
    public boolean isRelative() {
        return isRelative;
    }
}
