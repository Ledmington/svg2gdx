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

import java.util.function.Consumer;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Main {
    public static void main(final String[] args) {
        String filename = null;
        boolean showcase = false;

        for (final String arg : args) {
            switch (arg) {
                case "-h", "--help" -> {
                    System.out.println(String.join(
                            "\n",
                            "",
                            " svg2gdx - A converter from SVG to libGDX ShapeRenderer code.",
                            "",
                            "Usage: java -jar svg2gdx.jar [flags] FILE",
                            "",
                            "Flags:",
                            " -h, --help  Shows this help message and exits.",
                            " --test      Launches a sample libGDX app to see the converted image.",
                            "",
                            " FILE        The name of the .svg file to convert.",
                            ""));
                    System.exit(0);
                }
                case "--test" -> showcase = true;
                default -> {
                    if (filename != null) {
                        System.err.println("Cannot set the filename twice.");
                        System.exit(-1);
                    }
                    filename = arg;
                }
            }
        }

        if (filename == null) {
            System.err.println("You have not set the filename to convert.");
            System.exit(-1);
        }

        final SVGImage parsed = new SVGImage(filename);

        if (showcase) {
            Showcase.run(new Consumer<>() {

                private static final int MAX_ITERATIONS = 100;
                private int it = 0;
                private long totalTime = 0L;

                @Override
                public void accept(final ShapeRenderer sr) {
                    it++;
                    final long start = System.nanoTime();
                    parsed.draw(sr);
                    final long end = System.nanoTime();
                    totalTime += (end - start);
                    if (it >= MAX_ITERATIONS) {
                        final double averageNanos = (double) totalTime / (double) MAX_ITERATIONS;
                        System.out.printf(
                                "Drawing the image %,d times took %,d ns (%.3f ms) on average. At 60 FPS you could draw this image %,d times per frame.\n",
                                MAX_ITERATIONS, (long) averageNanos, averageNanos / 1_000_000.0, (long)
                                        ((1.0 / 60.0) / (averageNanos / 1_000_000_000.0)));
                        it = 0;
                        totalTime = 0L;
                    }
                }
            });
        } else {
            System.out.println(parsed.toGDXShapeRenderer());
        }
    }
}
