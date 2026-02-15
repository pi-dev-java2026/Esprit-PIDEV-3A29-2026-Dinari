package com.gestion.utils;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class PanierStorage {

    private static final String KEY_COURS = "panier_cours_ids";
    private static final Preferences prefs = Preferences.userNodeForPackage(PanierStorage.class);

    public static Set<Integer> getCoursIds() {
        String s = prefs.get(KEY_COURS, "");
        Set<Integer> ids = new LinkedHashSet<>();
        if (s == null || s.isBlank()) return ids;

        for (String part : s.split(",")) {
            part = part.trim();
            if (!part.isEmpty()) {
                try { ids.add(Integer.parseInt(part)); } catch (Exception ignored) {}
            }
        }
        return ids;
    }

    public static void addCoursId(int id) {
        Set<Integer> ids = getCoursIds();
        ids.add(id);
        saveCoursIds(ids);
    }

    public static void clearCours() {
        prefs.remove(KEY_COURS);
    }

    private static void saveCoursIds(Set<Integer> ids) {
        String s = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        prefs.put(KEY_COURS, s);
    }
    public static void removeCours(int id) {
        Set<Integer> ids = getCoursIds();
        ids.remove(id);
        String s = ids.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
        prefs.put(KEY_COURS, s);
    }

}
