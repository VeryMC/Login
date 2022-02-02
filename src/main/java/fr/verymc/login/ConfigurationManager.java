package fr.verymc.login;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;

public class ConfigurationManager {

    private static final HashMap<String, Path> PATHS = new HashMap<>();

    public ConfigurationManager(Path dataDirectory) throws IOException {
        PATHS.put("/", this.createDir(dataDirectory));

        //Menus
        //PATHS.put("menus", this.createDir(PATHS.get("/").resolve("menus")));
        //this.copy("menus/choose.json", PATHS.get("menus").resolve("choose.json"));
        //this.copy("menus/method.json", PATHS.get("menus").resolve("method.json"));

        //Messages
        PATHS.put("messages", this.createDir(PATHS.get("/").resolve("messages")));
        this.copy("messages/choose.json", PATHS.get("messages").resolve("choose.json"));
        this.copy("messages/method.json", PATHS.get("messages").resolve("method.json"));

        PATHS.put("messages/errors", this.createDir(PATHS.get("messages").resolve("messages")));
        this.copy("messages/errors/missingPasswordArg.json", PATHS.get("messages/errors").resolve("missingPasswordArg.json"));
    }

    private Path createDir(Path path) {
        if(Files.exists(path)) path.toFile().mkdir();
        return path;
    }

    private void copy(String resourcePath, Path targetPath) throws IOException {
        Files.copy(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream(resourcePath)),
                targetPath.toAbsolutePath());
    }

    public static String getJson(String folder, String name) throws IOException {
        return new String(Files.readAllBytes(PATHS.get(folder).resolve("name")));
    }

    public static Component getComponent(String folder, String name) {
        try {
            return GsonComponentSerializer.gson().deserialize(getJson(folder, name));
        } catch (IOException e) {
            return Component.text("§cImpossible de récupérer le contenu de la configuration !");
        }
    }
}
