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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** A collection of all the colors which appear in an SVGImage. */
public final class SVGPalette implements SVGElement {

    static final class SVGPaletteBuilder {

        // TODO: refactor with a BiMap
        private final Map<SVGColor, String> fromColorToName = new HashMap<>();
        private final Map<String, SVGColor> fromNameToColor = new HashMap<>();
        private int id = 0;

        public SVGPaletteBuilder() {}

        public void add(final SVGColor color) {
            Objects.requireNonNull(color);
            if (fromColorToName.containsKey(color)) {
                return;
            }
            final String name = "c" + id;
            fromColorToName.put(color, name);
            fromNameToColor.put(name, color);
            id++;
        }

        public String getName(final SVGColor color) {
            Objects.requireNonNull(color);
            if (!fromColorToName.containsKey(color)) {
                throw new IllegalArgumentException(String.format("Unknown color '%s'", color));
            }
            return fromColorToName.get(color);
        }

        public SVGPalette build() {
            return new SVGPalette(fromColorToName, fromNameToColor);
        }
    }

    static SVGPaletteBuilder builder() {
        return new SVGPaletteBuilder();
    }

    // TODO: refactor with a BiMap
    private final Map<SVGColor, String> fromColorToName;
    private final Map<String, SVGColor> fromNameToColor;

    private SVGPalette(final Map<SVGColor, String> fromColorToName, final Map<String, SVGColor> fromNameToColor) {
        Objects.requireNonNull(fromColorToName);
        Objects.requireNonNull(fromNameToColor);
        if (fromColorToName.size() != fromNameToColor.size()) {
            throw new IllegalArgumentException(
                    String.format("Different sizes: %,d and %,d", fromColorToName.size(), fromNameToColor.size()));
        }
        this.fromColorToName = Collections.unmodifiableMap(fromColorToName);
        this.fromNameToColor = Collections.unmodifiableMap(fromNameToColor);
    }

    /**
     * Returns a cached color given its name.
     *
     * @param colorName The name of the color to look for.
     * @return The corresponding color.
     */
    public SVGColor getFromName(final String colorName) {
        Objects.requireNonNull(colorName);
        if (!fromNameToColor.containsKey(colorName)) {
            throw new IllegalArgumentException(String.format("Unknown color name '%s'", colorName));
        }
        return fromNameToColor.get(colorName);
    }

    @Override
    public String toGDXShapeRenderer() {
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<SVGColor, String> e : fromColorToName.entrySet()) {
            sb.append(String.format(
                            "final Color %s = %s", e.getValue(), e.getKey().toGDXShapeRenderer()))
                    .append('\n');
        }
        return sb.toString();
    }
}