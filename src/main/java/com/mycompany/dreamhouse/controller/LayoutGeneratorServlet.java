package com.mycompany.dreamhouse.controller;

import com.mycompany.dreamhouse.generator.LayoutGenerator;
import com.mycompany.dreamhouse.model.LayoutDesign;
import com.mycompany.dreamhouse.model.LayoutItem;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebServlet("/layout/generate")
public class LayoutGeneratorServlet extends HttpServlet {
    private static final Set<String> CATEGORIES = Set.of("House", "Room", "Apartment", "Shop", "Restaurant",
            "Office", "Event Hall", "Classroom", "Garden", "Warehouse");
    private final LayoutGenerator generator = new LayoutGenerator();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        if (request.getSession(false) == null || request.getSession(false).getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Please log in before generating layouts.\"}");
            return;
        }
        String category = request.getParameter("category");
        if (!CATEGORIES.contains(category)) {
            badRequest(response, "Choose a valid category.");
            return;
        }
        try {
            double width = dimension(request.getParameter("width"), "Width");
            double length = dimension(request.getParameter("length"), "Length");
            double ratio = width / length;
            if (ratio < 0.25 || ratio > 4.0) {
                badRequest(response, "Width and length are too different for this conceptual layout. Keep their ratio between 1:4 and 4:1.");
                return;
            }
            Map<String, String> input = new LinkedHashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                if (values != null && values.length > 0) input.put(key, values[0]);
            });
            List<LayoutDesign> designs = generator.generate(category, input);
            writeResponse(response, category, width, length, designs);
        } catch (IllegalArgumentException exception) {
            badRequest(response, exception.getMessage());
        }
    }

    private static double dimension(String raw, String label) {
        try {
            double value = Double.parseDouble(raw);
            if (!Double.isFinite(value) || value < 4 || value > 500) {
                throw new IllegalArgumentException(label + " must be between 4 and 500 feet.");
            }
            return value;
        } catch (NumberFormatException | NullPointerException exception) {
            throw new IllegalArgumentException(label + " must be a number.");
        }
    }

    private static void badRequest(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("{\"error\":\"" + escape(message) + "\"}");
    }

    private static void writeResponse(HttpServletResponse response, String category, double width,
            double length, List<LayoutDesign> designs) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("{\"category\":\"").append(escape(category)).append("\",\"width\":")
                .append(width).append(",\"length\":").append(length).append(",\"designs\":[");
        for (int d = 0; d < designs.size(); d++) {
            if (d > 0) json.append(',');
            LayoutDesign design = designs.get(d);
            json.append("{\"name\":\"").append(escape(design.name())).append("\",\"description\":\"")
                    .append(escape(design.description())).append("\",\"items\":[");
            for (int i = 0; i < design.items().size(); i++) {
                if (i > 0) json.append(',');
                LayoutItem item = design.items().get(i);
                json.append("{\"label\":\"").append(escape(item.label())).append("\",\"x\":")
                        .append(item.x()).append(",\"y\":").append(item.y()).append(",\"width\":")
                        .append(item.width()).append(",\"height\":").append(item.height()).append('}');
            }
            json.append("]}");
        }
        json.append("]}");
        response.getWriter().write(json.toString());
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
