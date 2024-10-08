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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.ledmington.svg2gdx.path.SVGPath;

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

        final SVGPalette palette = new SVGPalette();

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

        return new SVGImage(imageWidth, imageHeight, palette, elements);
    }

    private static SVGRectangle convertRect(final Node node, final SVGPalette palette) {
        final NamedNodeMap m = node.getAttributes();

        final double rectWidth = Double.parseDouble(m.getNamedItem("width").getNodeValue());
        final double rectHeight = Double.parseDouble(m.getNamedItem("height").getNodeValue());
        final double rectX = Double.parseDouble(m.getNamedItem("x").getNodeValue());
        final double rectY = Double.parseDouble(m.getNamedItem("y").getNodeValue());

        final String style = m.getNamedItem("style").getNodeValue();
        final Map<String, String> styleValues =
                Arrays.stream(style.split(";")).collect(Collectors.toMap(s -> s.split(":")[0], s -> s.split(":")[1]));

        final SVGColor color = parseColor(styleValues, palette);
        final boolean filled = styleValues.containsKey("fill");

        return new SVGRectangle(rectX, rectY, rectWidth, rectHeight, filled, palette.getName(color));
    }

    private static SVGPath convertPath(final Node node, final SVGPalette palette) {
        final NamedNodeMap m = node.getAttributes();

        if (m.getNamedItem("d") == null) {
            throw new IllegalArgumentException("Expected a 'd' attribute for 'path' element");
        }

        final String style = m.getNamedItem("style").getNodeValue();
        final Map<String, String> styleValues =
                Arrays.stream(style.split(";")).collect(Collectors.toMap(s -> s.split(":")[0], s -> s.split(":")[1]));

        final SVGColor color = parseColor(styleValues, palette);
        final String colorName = palette.getName(color);

        return new SVGPath(m.getNamedItem("d").getNodeValue(), colorName);
    }

    private static SVGColor parseColor(final Map<String, String> styleValues, final SVGPalette palette) {
        if (!styleValues.containsKey("fill")) {
            return new SVGColor();
        }

        final String hexColor = styleValues.get("fill").substring(1); // removing the starting '#'
        final byte r = ParseUtils.parseByteHex(hexColor.substring(0, 2));
        final byte g = ParseUtils.parseByteHex(hexColor.substring(2, 4));
        final byte b = ParseUtils.parseByteHex(hexColor.substring(4, 6));
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
