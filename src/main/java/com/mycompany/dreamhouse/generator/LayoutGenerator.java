package com.mycompany.dreamhouse.generator;

import com.mycompany.dreamhouse.model.LayoutDesign;
import com.mycompany.dreamhouse.model.LayoutItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LayoutGenerator {
    private static final String[] DESIGN_NAMES = {
        "Perimeter zones", "Window-side plan", "Open-center flow", "Quiet / service wing", "Balanced circulation", "Corner-focused plan"
    };
    private static final String[] DESCRIPTIONS = {
        "Places supporting spaces around a clear main area.",
        "Moves the primary spaces toward the window side.",
        "Keeps a more open center for easier movement.",
        "Groups quieter or service areas along one side.",
        "Balances shared spaces and the circulation route.",
        "Uses an alternate corner-first arrangement."
    };

    public List<LayoutDesign> generate(String category, Map<String, String> input) {
        List<String> spaces = makeSpaces(category, input);
        List<LayoutDesign> designs = new ArrayList<>();
        for (int index = 0; index < DESIGN_NAMES.length; index++) {
            List<String> order = arrange(spaces, index);
            designs.add(new LayoutDesign("Design " + (index + 1) + " · " + DESIGN_NAMES[index],
                    DESCRIPTIONS[index], place(order, index)));
        }
        return designs;
    }

    private List<String> makeSpaces(String category, Map<String, String> p) {
        List<String> s = new ArrayList<>();
        switch (category) {
            case "House", "Apartment" -> {
                addCount(s, "Bedroom", count(p, "bedrooms", 2, 1, 8));
                addCount(s, "Bathroom", count(p, "bathrooms", 1, 0, 6));
                addFlag(s, p, "kitchen", "Kitchen", true);
                addFlag(s, p, "living", "Living room", true);
                addFlag(s, p, "dining", "Dining area", true);
                if ("House".equals(category)) addFlag(s, p, "parking", "Parking", false);
                else addFlag(s, p, "balcony", "Balcony", true);
            }
            case "Room" -> {
                addFlag(s, p, "bed", "Bed", true);
                addFlag(s, p, "wardrobe", "Wardrobe", true);
                addFlag(s, p, "studyTable", "Study table", true);
                addFlag(s, p, "sofa", "Sofa", false);
                addFlag(s, p, "tv", "TV unit", true);
            }
            case "Shop" -> {
                addCount(s, "Rack", count(p, "racks", 4, 0, 12));
                addFlag(s, p, "storage", "Storage", true);
                addFlag(s, p, "billing", "Billing counter", true);
                addFlag(s, p, "display", "Display area", true);
                addFlag(s, p, "entrance", "Entrance", true);
            }
            case "Restaurant" -> {
                s.add("Dining for " + count(p, "seats", 24, 4, 300) + " seats");
                addCount(s, "Table zone", count(p, "tables", 6, 1, 40));
                addFlag(s, p, "kitchen", "Kitchen", true);
                addFlag(s, p, "counter", "Service counter", true);
                addFlag(s, p, "washroom", "Washroom", true);
                addFlag(s, p, "entrance", "Entrance", true);
            }
            case "Office" -> {
                s.add("Work area · " + count(p, "employees", 8, 1, 100) + " employees");
                addCount(s, "Cabin", count(p, "cabins", 2, 0, 12));
                addFlag(s, p, "meeting", "Meeting room", true);
                addFlag(s, p, "reception", "Reception", true);
                addFlag(s, p, "pantry", "Pantry", false);
            }
            case "Event Hall" -> {
                s.add("Guest seating · " + count(p, "guests", 100, 10, 1000) + " guests");
                addFlag(s, p, "stage", "Stage", true);
                addFlag(s, p, "dining", "Dining area", true);
                addFlag(s, p, "dj", "DJ / entertainment", true);
                addFlag(s, p, "entrance", "Entrance", true);
            }
            case "Classroom" -> {
                s.add("Student desks · " + count(p, "students", 30, 5, 120) + " students");
                addFlag(s, p, "board", "Board / teaching wall", true);
                addFlag(s, p, "teacher", "Teacher area", true);
                addFlag(s, p, "storage", "Storage", true);
                addFlag(s, p, "entrance", "Entrance", true);
            }
            case "Garden" -> {
                addFlag(s, p, "lawn", "Lawn", true);
                addCount(s, "Plant zone", count(p, "plantZones", 4, 1, 12));
                addFlag(s, p, "seating", "Seating", true);
                addFlag(s, p, "pathways", "Pathways", true);
                addFlag(s, p, "water", "Water feature", false);
            }
            case "Warehouse" -> {
                addCount(s, "Storage zone", count(p, "storageZones", 5, 1, 16));
                addFlag(s, p, "loading", "Loading bay", true);
                addFlag(s, p, "packing", "Packing area", true);
                addFlag(s, p, "office", "Office", true);
                addFlag(s, p, "aisle", "Main aisle", true);
            }
            default -> throw new IllegalArgumentException("Choose one of the 10 available categories.");
        }
        s.add("Circulation space");
        while (s.size() < 3) s.add("Open area " + (s.size() - 1));
        if (s.size() > 24) throw new IllegalArgumentException("Reduce the requested room or area counts.");
        return s;
    }

    private static void addFlag(List<String> spaces, Map<String, String> values, String key, String label, boolean defaultOn) {
        if (flag(values, key, defaultOn)) spaces.add(label);
    }

    private static void addCount(List<String> spaces, String label, int count) {
        for (int i = 1; i <= count; i++) spaces.add(label + " " + i);
    }

    private static boolean flag(Map<String, String> values, String key, boolean defaultOn) {
        String value = values.get(key);
        return value == null ? defaultOn : value.equalsIgnoreCase("true") || value.equalsIgnoreCase("on") || value.equals("1");
    }

    private static int count(Map<String, String> values, String key, int fallback, int minimum, int maximum) {
        String raw = values.get(key);
        if (raw == null || raw.isBlank()) return fallback;
        try {
            int value = Integer.parseInt(raw);
            if (value < minimum || value > maximum) throw new IllegalArgumentException(key + " must be between " + minimum + " and " + maximum + ".");
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(key + " must be a whole number.");
        }
    }

    private static List<String> arrange(List<String> base, int variation) {
        List<String> result = new ArrayList<>(base);
        int n = result.size();
        switch (variation) {
            case 1 -> Collections.reverse(result);
            case 2 -> Collections.rotate(result, -(n / 2));
            case 3 -> {
                List<String> alternate = new ArrayList<>();
                for (int i = 0; i < n; i += 2) alternate.add(result.get(i));
                for (int i = 1; i < n; i += 2) alternate.add(result.get(i));
                result = alternate;
            }
            case 4 -> Collections.rotate(result, 1);
            case 5 -> {
                List<String> alternate = new ArrayList<>();
                for (int i = n - 1; i >= 0; i -= 2) alternate.add(result.get(i));
                for (int i = n - 2; i >= 0; i -= 2) alternate.add(result.get(i));
                result = alternate;
            }
            default -> { }
        }
        return result;
    }

    private static List<LayoutItem> place(List<String> order, int variation) {
        int count = order.size();
        int columns = switch (variation) {
            case 0 -> Math.max(1, (int) Math.ceil(Math.sqrt(count)));
            case 1 -> Math.min(3, Math.max(2, (int) Math.ceil(count / 2.0)));
            case 2 -> Math.min(2, count);
            case 3 -> Math.min(3, count);
            case 5 -> Math.min(4, count);
            default -> Math.min(2, count);
        };
        int rows = (int) Math.ceil((double) count / columns);
        double cellHeight = 100.0 / rows;
        List<LayoutItem> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int row = i / columns;
            int cellsInRow = Math.min(columns, count - row * columns);
            if (i % columns != 0) continue;
            double rowWidth = 100.0 * cellsInRow / columns;
            double x = (100.0 - rowWidth) / 2.0;
            double totalWeight = 0.0;
            for (int column = 0; column < cellsInRow; column++) totalWeight += spaceWeight(order.get(i + column));
            for (int column = 0; column < cellsInRow; column++) {
                String label = order.get(i + column);
                double cellWidth = rowWidth * spaceWeight(label) / totalWeight;
                items.add(new LayoutItem(label, x, row * cellHeight, cellWidth, cellHeight));
                x += cellWidth;
            }
        }
        return items;
    }

    private static double spaceWeight(String label) {
        String name = label.toLowerCase();
        if (name.contains("bath") || name.contains("washroom") || name.contains("wardrobe")
                || name.contains("storage") || name.contains("entrance") || name.contains("tv unit")) return 0.7;
        if (name.contains("living") || name.contains("work area") || name.contains("guest seating")
                || name.contains("dining") || name.contains("lawn") || name.contains("loading")) return 1.6;
        if (name.contains("bed") || name.contains("kitchen") || name.contains("meeting")
                || name.contains("stage") || name.contains("parking") || name.contains("table zone")) return 1.3;
        if (name.contains("circulation") || name.contains("pathway") || name.contains("aisle")) return 0.9;
        return 1.0;
    }
}
