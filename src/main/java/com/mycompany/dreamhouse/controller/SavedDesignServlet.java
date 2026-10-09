package com.mycompany.dreamhouse.controller;

import com.mycompany.dreamhouse.dao.SavedDesignDao;
import com.mycompany.dreamhouse.generator.LayoutGenerator;
import com.mycompany.dreamhouse.model.LayoutDesign;
import com.mycompany.dreamhouse.model.LayoutItem;
import com.mycompany.dreamhouse.model.SavedDesign;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebServlet("/layout/saved")
public class SavedDesignServlet extends HttpServlet {
    private static final Set<String> CATEGORIES = Set.of("House", "Room", "Apartment", "Shop", "Restaurant",
            "Office", "Event Hall", "Classroom", "Garden", "Warehouse");
    private final SavedDesignDao savedDesigns = new SavedDesignDao();
    private final LayoutGenerator generator = new LayoutGenerator();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long userId = userId(request, response);
        if (userId == null) return;
        try {
            List<SavedDesign> designs = savedDesigns.findForUser(userId);
            StringBuilder json = new StringBuilder("{\"designs\":[");
            for (int i = 0; i < designs.size(); i++) {
                if (i > 0) json.append(',');
                SavedDesign design = designs.get(i);
                json.append("{\"id\":").append(design.id())
                        .append(",\"category\":\"").append(escape(design.category()))
                        .append("\",\"width\":").append(design.width())
                        .append(",\"length\":").append(design.length())
                        .append(",\"name\":\"").append(escape(design.name()))
                        .append("\",\"description\":\"").append(escape(design.description()))
                        .append("\",\"createdAt\":\"").append(escape(design.createdAt()))
                        .append("\",\"items\":").append(design.itemsJson()).append('}');
            }
            json.append("]}");
            respond(response, HttpServletResponse.SC_OK, json.toString());
        } catch (SQLException exception) {
            getServletContext().log("Could not load saved DreamHouse layouts", exception);
            error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not load saved designs.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        Long userId = userId(request, response);
        if (userId == null) return;
        String category = request.getParameter("category");
        if (!CATEGORIES.contains(category)) {
            error(response, HttpServletResponse.SC_BAD_REQUEST, "Choose a valid category.");
            return;
        }
        try {
            double width = dimension(request.getParameter("width"), "Width");
            double length = dimension(request.getParameter("length"), "Length");
            if (width / length < 0.25 || width / length > 4.0) {
                error(response, HttpServletResponse.SC_BAD_REQUEST, "Keep the width and length ratio between 1:4 and 4:1.");
                return;
            }
            int designIndex = Integer.parseInt(request.getParameter("designIndex"));
            Map<String, String> input = new LinkedHashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                if (values != null && values.length > 0) input.put(key, values[0]);
            });
            List<LayoutDesign> designs = generator.generate(category, input);
            if (designIndex < 0 || designIndex >= designs.size()) {
                error(response, HttpServletResponse.SC_BAD_REQUEST, "Choose one of the six generated ideas.");
                return;
            }
            LayoutDesign design = designs.get(designIndex);
            long id = savedDesigns.save(userId, category, width, length, design.name(),
                    design.description(), itemsJson(design.items()));
            respond(response, HttpServletResponse.SC_CREATED,
                    "{\"id\":" + id + ",\"message\":\"Design saved to your account.\"}");
        } catch (NumberFormatException exception) {
            error(response, HttpServletResponse.SC_BAD_REQUEST, "Enter valid dimensions and choose a design.");
        } catch (IllegalArgumentException exception) {
            error(response, HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        } catch (SQLException exception) {
            getServletContext().log("Could not save a DreamHouse layout", exception);
            error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not save the design. Please try again.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long userId = userId(request, response);
        if (userId == null) return;
        try {
            long id = Long.parseLong(request.getParameter("id"));
            if (id < 1) throw new NumberFormatException();
            if (!savedDesigns.delete(userId, id)) {
                error(response, HttpServletResponse.SC_NOT_FOUND, "Saved design not found.");
                return;
            }
            respond(response, HttpServletResponse.SC_OK, "{\"message\":\"Saved design deleted.\"}");
        } catch (NumberFormatException exception) {
            error(response, HttpServletResponse.SC_BAD_REQUEST, "Choose a valid saved design.");
        } catch (SQLException exception) {
            getServletContext().log("Could not delete a saved DreamHouse layout", exception);
            error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not delete the design.");
        }
    }

    private static Long userId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Object value = request.getSession(false) == null ? null
                : request.getSession(false).getAttribute("userId");
        if (value instanceof Number number) return number.longValue();
        error(response, HttpServletResponse.SC_UNAUTHORIZED, "Please log in to use saved designs.");
        return null;
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

    private static String itemsJson(List<LayoutItem> items) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) json.append(',');
            LayoutItem item = items.get(i);
            json.append("{\"label\":\"").append(escape(item.label())).append("\",\"x\":")
                    .append(item.x()).append(",\"y\":").append(item.y())
                    .append(",\"width\":").append(item.width())
                    .append(",\"height\":").append(item.height()).append('}');
        }
        return json.append(']').toString();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private static void error(HttpServletResponse response, int status, String message) throws IOException {
        respond(response, status, "{\"error\":\"" + escape(message) + "\"}");
    }

    private static void respond(HttpServletResponse response, int status, String json) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(json);
    }
}
