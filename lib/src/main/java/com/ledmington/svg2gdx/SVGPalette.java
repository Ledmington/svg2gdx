/*
 * svg2gdx - A converter from SVG to LibGDX ShapeRenderer code.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class SVGPalette implements SVGElement {

    private int id = 0;
    private final Map<SVGColor, Integer> colors = new HashMap<>();

    public void add(final SVGColor color) {
        if (colors.containsKey(color)) {
            return;
        }
        colors.put(Objects.requireNonNull(color), id++);
    }

    public String getName(final SVGColor color) {
        if (!colors.containsKey(Objects.requireNonNull(color))) {
            throw new IllegalArgumentException(String.format("Color '%s' not existing", color));
        }
        return "c" + colors.get(color);
    }

    @Override
    public String toGDXShapeRenderer() {
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<SVGColor, Integer> e : colors.entrySet()) {
            sb.append(String.format(
                    "final Color %s = %s\n", "c" + e.getValue(), e.getKey().toGDXShapeRenderer()));
        }
        return sb.toString();
    }
}
