package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;

public class GsonUtil {
    
    private static final Gson GSON_INSTANCE = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();

    /**
     * Get a singleton Gson instance configured with custom type adapters
     * for Java 17 compatibility (LocalDateTime, etc.)
     */
    public static Gson getGson() {
        return GSON_INSTANCE;
    }
    
    private GsonUtil() {
        // Prevent instantiation
    }
}
