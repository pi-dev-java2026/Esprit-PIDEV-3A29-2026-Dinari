package tn.esprit.utils;

import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire de logos pour les abonnements
 * Fournit les vraies images des services
 */
public class LogoManager {

    private static final Map<String, String> LOGO_URLS = new HashMap<>();
    private static final Map<String, Image> LOGO_CACHE = new HashMap<>();

    static {
        // URLs des logos réels (depuis des CDN publics)
        LOGO_URLS.put("Netflix", "https://cdn.worldvectorlogo.com/logos/netflix-3.svg");
        LOGO_URLS.put("Spotify", "https://cdn.worldvectorlogo.com/logos/spotify-2.svg");
        LOGO_URLS.put("Disney+", "https://cdn.worldvectorlogo.com/logos/disney.svg");
        LOGO_URLS.put("Amazon Prime", "https://cdn.worldvectorlogo.com/logos/amazon-prime-video.svg");
        LOGO_URLS.put("YouTube Premium", "https://cdn.worldvectorlogo.com/logos/youtube-icon.svg");
        LOGO_URLS.put("Apple Music", "https://cdn.worldvectorlogo.com/logos/apple-music-1.svg");
        LOGO_URLS.put("Adobe", "https://cdn.worldvectorlogo.com/logos/adobe-2.svg");
        LOGO_URLS.put("Microsoft 365", "https://cdn.worldvectorlogo.com/logos/microsoft-office-2019.svg");
        LOGO_URLS.put("Dropbox", "https://cdn.worldvectorlogo.com/logos/dropbox-1.svg");
        LOGO_URLS.put("iCloud", "https://cdn.worldvectorlogo.com/logos/icloud-1.svg");
        LOGO_URLS.put("PlayStation Plus", "https://cdn.worldvectorlogo.com/logos/playstation.svg");
        LOGO_URLS.put("Xbox Game Pass", "https://cdn.worldvectorlogo.com/logos/xbox-2.svg");
        LOGO_URLS.put("LinkedIn Premium", "https://cdn.worldvectorlogo.com/logos/linkedin-icon-2.svg");
        LOGO_URLS.put("Canva Pro", "https://cdn.worldvectorlogo.com/logos/canva-1.svg");
        LOGO_URLS.put("Notion", "https://cdn.worldvectorlogo.com/logos/notion-1.svg");
    }

    /**
     * Obtenir l'image du logo pour un service
     * @param serviceName Nom du service
     * @param size Taille du logo en pixels
     * @return Image du logo
     */
    public static Image getLogoImage(String serviceName, double size) {
        String cacheKey = serviceName + "_" + size;

        // Vérifier le cache
        if (LOGO_CACHE.containsKey(cacheKey)) {
            return LOGO_CACHE.get(cacheKey);
        }

        // Charger l'image
        String url = LOGO_URLS.getOrDefault(serviceName, null);
        Image image;

        if (url != null) {
            try {
                image = new Image(url, size, size, true, true, true);
                LOGO_CACHE.put(cacheKey, image);
            } catch (Exception e) {
                // En cas d'erreur, utiliser un placeholder
                image = createPlaceholder(serviceName, size);
            }
        } else {
            image = createPlaceholder(serviceName, size);
        }

        return image;
    }

    /**
     * Créer un placeholder simple si l'image n'est pas disponible
     */
    private static Image createPlaceholder(String serviceName, double size) {
        // Pour l'instant, retourner null - le contrôleur utilisera un emoji
        return null;
    }

    /**
     * Obtenir la couleur principale associée au service
     */
    public static String getServiceColor(String serviceName) {
        Map<String, String> colors = new HashMap<>();
        colors.put("Netflix", "#E50914");
        colors.put("Spotify", "#1DB954");
        colors.put("Disney+", "#113CCF");
        colors.put("Amazon Prime", "#00A8E1");
        colors.put("YouTube Premium", "#FF0000");
        colors.put("Apple Music", "#FA243C");
        colors.put("Adobe", "#FF0000");
        colors.put("Microsoft 365", "#0078D4");
        colors.put("Dropbox", "#0061FF");
        colors.put("iCloud", "#3693F3");
        colors.put("PlayStation Plus", "#003087");
        colors.put("Xbox Game Pass", "#107C10");
        colors.put("LinkedIn Premium", "#0A66C2");
        colors.put("Canva Pro", "#00C4CC");
        colors.put("Notion", "#000000");

        return colors.getOrDefault(serviceName, "#5B47FB");
    }

    /**
     * Vérifier si un logo est disponible
     */
    public static boolean hasLogo(String serviceName) {
        return LOGO_URLS.containsKey(serviceName);
    }
}