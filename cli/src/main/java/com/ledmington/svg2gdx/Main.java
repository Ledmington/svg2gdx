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

import org.openjdk.jol.info.GraphLayout;

public class Main {
    public static void main(final String[] args) {
        String filename = null;
        boolean showcase = false;
        int width = 1280;
        int height = 720;

        for (final String arg : args) {
            if (arg.equals("-h") || arg.equals("--help")) {
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
                        " --width=W   Width in pixels of the sample app screen. Only available in combination with '--test'. Default: 1280.",
                        " --height=H  Height in pixels of the sample app screen. Only available in combination with '--test'. Default: 720.",
                        "",
                        " FILE        The name of the .svg file to convert.",
                        ""));
                System.exit(0);
            } else if (arg.equals("--test")) {
                showcase = true;
            } else if (arg.startsWith("--width=")) {
                if (!showcase) {
                    System.err.println("WARNING: Argument '--width' needs '--test' to work.");
                }
                final String value = arg.substring("--width=".length());
                if (!value.chars().allMatch(Character::isDigit)) {
                    System.err.printf("Expected an integer after '--width=' but was '%s'%n", value);
                    System.exit(-1);
                }
                width = Integer.parseInt(value);
            } else if (arg.startsWith("--height=")) {
                if (!showcase) {
                    System.err.println("WARNING: Argument '--height' needs '--test' to work.");
                }
                final String value = arg.substring("--height=".length());
                if (!value.chars().allMatch(Character::isDigit)) {
                    System.err.printf("Expected an integer after '--height=' but was '%s'%n", value);
                    System.exit(-1);
                }
                height = Integer.parseInt(value);
            } else {
                if (filename != null) {
                    System.err.println("Cannot set the filename twice.");
                    System.exit(-1);
                }
                filename = arg;
            }
        }

        if (filename == null) {
            System.err.println("You have not set the filename to convert.");
            System.exit(-1);
        }

        // Needed to make jol's GraphLayout work
        System.setProperty("jol.magicFieldOffset", "true");

        final SVGImage parsed = SVGParser.parseImage(filename);
        System.out.printf(
                "One runtime instance of this image occupies %,d bytes.%n",
                GraphLayout.parseInstance(parsed).totalSize());

        if (showcase) {
            Showcase.run(width, height, new Consumer<>() {

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
                                "Drawing the image %,d times took %,d ns (%.3f ms) on average. At 60 FPS you could draw this image %,d times per frame.%n",
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
