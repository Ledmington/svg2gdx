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

public final class Group implements Element {

    private final Style style;
    private final List<Element> elements;

    public Group(final Style style, final List<Element> elements) {
        this.style = Objects.requireNonNull(style);
        Objects.requireNonNull(elements);
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("Useless group with no elements inside");
        }
        this.elements = Collections.unmodifiableList(elements);
    }

    @Override
    public String toGDXShapeRenderer() {
        throw new Error("Not implemented");
    }

    @Override
    public String toString() {
        return "Group(style=" + style + ";elements=" + elements + ')';
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 31 * h + style.hashCode();
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
        if (!getClass().equals(other.getClass())) {
            return false;
        }
        final Group g = (Group) other;
        return this.style.equals(g.style) && this.elements.equals(g.elements);
    }
}
