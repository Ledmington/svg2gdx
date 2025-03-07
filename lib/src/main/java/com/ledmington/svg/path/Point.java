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
package com.ledmington.svg.path;

/**
 * A point in an SVG path element.
 *
 * @param x The x coordinate.
 * @param y The y coordinate.
 */
public record Point(double x, double y) {

    /**
     * Adds the coordinates of the given point to this point and returns a new instance.
     *
     * @param p The point to be added.
     * @return A new point with the new coordinates.
     */
    public Point add(final Point p) {
        return new Point(this.x + p.x, this.y + p.y);
    }
}
