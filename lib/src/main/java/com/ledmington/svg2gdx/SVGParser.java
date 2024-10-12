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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.ledmington.svg2gdx.path.SVGPath;
import com.ledmington.svg2gdx.path.SVGPathBezierAbsolute;
import com.ledmington.svg2gdx.path.SVGPathBezierRelative;
import com.ledmington.svg2gdx.path.SVGPathClosepath;
import com.ledmington.svg2gdx.path.SVGPathElement;
import com.ledmington.svg2gdx.path.SVGPathLinetoAbsolute;
import com.ledmington.svg2gdx.path.SVGPathLinetoRelative;
import com.ledmington.svg2gdx.path.SVGPathMoveto;
import com.ledmington.svg2gdx.path.SVGPathPoint;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public final class SVGParser {

    private SVGParser() {}

    public static SVGImage parseImage(final String inputFilename) {
        final File inputFile = new File(inputFilename);
        final Document doc;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputFile);
        } catch (final ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
        doc.getDocumentElement().normalize();
        final Element root = doc.getDocumentElement();
        if (!root.getNodeName().equals("svg")) {
            throw new IllegalArgumentException(
                    String.format("Invalid root element: expected 'svg' but was '%s'", root.getNodeName()));
        }

        final double imageWidth =
                Double.parseDouble(root.getAttributes().getNamedItem("width").getNodeValue());
        final double imageHeight =
                Double.parseDouble(root.getAttributes().getNamedItem("height").getNodeValue());

        final SVGPalette.SVGPaletteBuilder palette = SVGPalette.builder();

        final List<SVGElement> elements = new ArrayList<>();
        final NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node node = children.item(i);
            switch (node.getNodeName()) {
                case "rect":
                    elements.add(convertRect(node, palette));
                    break;
                case "path":
                    elements.add(convertPath(node, palette));
                    break;
                case "defs":
                case "metadata":
                case "#text":
                    // we don't care about these
                    break;
                default:
                    throw new IllegalArgumentException(
                            String.format("Unknown element with name '%s'", node.getNodeName()));
            }
        }

        return new SVGImage(imageWidth, imageHeight, palette.build(), elements);
    }

    private static Map<String, String> parseStyle(final String style) {
        final Map<String, String> m = new HashMap<>();
        final String[] styles = style.split(";");
        for (final String s : styles) {
            final int idx = s.indexOf(':');
            final String key = s.substring(0, idx);
            switch (key) {
                case "fill", "fill-opacity", "stroke", "display" -> {}
                default -> throw new IllegalArgumentException(String.format("Unknown style element '%s'", key));
            }
            m.put(key, s.substring(idx + 1));
        }
        return m;
    }

    private static SVGRectangle convertRect(final Node node, final SVGPalette.SVGPaletteBuilder palette) {
        final NamedNodeMap m = node.getAttributes();

        final double rectWidth = Double.parseDouble(m.getNamedItem("width").getNodeValue());
        final double rectHeight = Double.parseDouble(m.getNamedItem("height").getNodeValue());
        final double rectX = Double.parseDouble(m.getNamedItem("x").getNodeValue());
        final double rectY = Double.parseDouble(m.getNamedItem("y").getNodeValue());

        final String style = m.getNamedItem("style").getNodeValue();
        final Map<String, String> styleValues = parseStyle(style);

        final SVGColor color = parseColor(styleValues, palette);
        final boolean filled = styleValues.containsKey("fill");

        return new SVGRectangle(rectX, rectY, rectWidth, rectHeight, filled, palette.getName(color));
    }

    private static SVGPath convertPath(final Node node, final SVGPalette.SVGPaletteBuilder palette) {
        final NamedNodeMap m = node.getAttributes();

        final String style = m.getNamedItem("style").getNodeValue();
        final Map<String, String> styleValues = parseStyle(style);

        final SVGColor color = parseColor(styleValues, palette);
        final String colorName = palette.getName(color);

        if (m.getNamedItem("d") == null) {
            throw new IllegalArgumentException("Expected a 'd' attribute for 'path' element");
        }

        return new SVGPath(parsePath(m.getNamedItem("d").getNodeValue()), colorName);
    }

    private static SVGPathPoint parsePathPoint(final String pointData) {
        final int idx = pointData.indexOf(',');
        return new SVGPathPoint(
                Double.parseDouble(pointData.substring(0, idx)), Double.parseDouble(pointData.substring(idx + 1)));
    }

    private static List<SVGPathElement> parsePath(final String pathString) {
        final List<SVGPathElement> elements = new ArrayList<>();
        final String[] pathData = pathString.split(" ");

        if (!pathData[0].equals("m") && !pathData[0].equals("M")) {
            throw new IllegalArgumentException(
                    String.format("Invalid path data: must start with 'm' or 'M' but was '%s'", pathData[0]));
        }

        for (int i = 0; i < pathData.length; i++) {
            final String elem = pathData[i];
            final SVGPathElement x =
                    switch (elem) {

                            // Relative/Absolute "moveto" command
                        case "m", "M" -> {
                            final boolean isRelative = elem.equals("m");

                            i++;
                            final SVGPathPoint initialPoint = parsePathPoint(pathData[i]);

                            final List<SVGPathPoint> implicitLines = new ArrayList<>();
                            if (i + 1 < pathData.length && pathData[i + 1].contains(",")) {
                                i++;
                                for (; i < pathData.length && pathData[i].contains(","); i++) {
                                    implicitLines.add(parsePathPoint(pathData[i]));
                                }
                            }

                            yield new SVGPathMoveto(isRelative, initialPoint, implicitLines);
                        }

                            // "Bezier" commands
                            // (https://www.w3.org/TR/SVG2/paths.html#PathDataCubicBezierCommands)
                        case "c" -> new SVGPathBezierRelative();
                        case "C" -> new SVGPathBezierAbsolute();

                            // "lineto" commands
                            // (https://www.w3.org/TR/SVG2/paths.html#PathDataLinetoCommands)
                        case "l" -> new SVGPathLinetoRelative();
                        case "L" -> new SVGPathLinetoAbsolute();

                            // "closepath" commands
                            // (https://www.w3.org/TR/SVG2/paths.html#PathDataClosePathCommand)
                        case "z", "Z" -> new SVGPathClosepath();

                        default -> throw new IllegalArgumentException(String.format("Unknown path element '%s'", elem));
                    };

            elements.add(x);
        }

        return elements;
    }

    private static SVGColor parseColor(
            final Map<String, String> styleValues, final SVGPalette.SVGPaletteBuilder palette) {
        if (!styleValues.containsKey("fill")) {
            return new SVGColor();
        }

        final String hexColor = styleValues.get("fill");
        final byte r = ParseUtils.parseByteHex(hexColor.substring(1, 3));
        final byte g = ParseUtils.parseByteHex(hexColor.substring(3, 5));
        final byte b = ParseUtils.parseByteHex(hexColor.substring(5, 7));

        byte a = (byte) 0xff;
        if (styleValues.containsKey("fill-opacity")) {
            final double opacity = Double.parseDouble(styleValues.get("fill-opacity"));
            if (opacity < 0.0 || opacity > 1.0) {
                throw new IllegalArgumentException(
                        String.format("Invalid opacity value: expected between 0.0 and 1.0 but was %f", opacity));
            }
            a = ParseUtils.asByte((int) (opacity * 255.0));
        }

        final SVGColor color = new SVGColor(r, g, b, a);
        palette.add(color);
        return color;
    }
}
