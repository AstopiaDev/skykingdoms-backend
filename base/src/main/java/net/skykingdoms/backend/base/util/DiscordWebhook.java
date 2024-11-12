package net.skykingdoms.backend.base.util;

import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.logging.Logging;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Class used to execute Discord Webhooks with low effort
 * <a href="https://gist.github.com/k3kdude/fba6f6b37594eae3d6f9475330733bdb">Source</a>
 *
 * @author k3kdude
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DiscordWebhook {

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public static final String STAFFCHAT_WEBHOOK = "https://discord.com/api/webhooks/1177641634272379010/m-XvV1Fkx21yOPNaxraxAvDJoDS2U-qv_3OSKYUn0ah4B8s1wpBPrRC7Yd8iTRgHwjbK";
    public static final String REPORTS_WEBHOOK = "https://discord.com/api/webhooks/1135875522593816606/1h_acn9zTUul23ZRNq0b4eXLYQ8LwK3vK_PgObfyG2jrRdgQPtvei5GvN3eZcNyB2X-u";
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    final String url;
    String content;
    String username;
    String avatarUrl;
    boolean tts;
    List<File> attachments = new ArrayList<>();
    final List<EmbedObject> embeds = new ArrayList<>();

    /**
     * Constructs a new DiscordWebhook instance
     *
     * @param url The webhook URL obtained in Discord
     */
    public DiscordWebhook(final String url) {
        this.url = url;
    }

    public DiscordWebhook setContent(final String content) {
        this.content = content;
        return this;
    }

    public DiscordWebhook addAttachments(List<File> attachments) {
        this.attachments = attachments;
        return this;
    }

    public DiscordWebhook setUsername(final String username) {
        this.username = username;
        return this;
    }

    public DiscordWebhook setAvatarUrl(final String avatarUrl) {
        this.avatarUrl = avatarUrl;
        return this;
    }

    public DiscordWebhook setTts(final boolean tts) {
        this.tts = tts;
        return this;
    }

    public DiscordWebhook addEmbed(final EmbedObject embed) {
        this.embeds.add(embed);
        return this;
    }

    public void execute() {
        if (this.content == null && this.embeds.isEmpty()) {
            throw new IllegalArgumentException("Set content or add at least one EmbedObject");
        }

        val json = new JSONObject();

        json.put("content", this.content);
        json.put("username", this.username);
        json.put("avatar_url", this.avatarUrl);
        json.put("tts", this.tts);

        if (!this.embeds.isEmpty()) {
            List<JSONObject> embedObjects = new ArrayList<>();

            for (val embed : this.embeds) {
                val jsonEmbed = new JSONObject();

                jsonEmbed.put("title", embed.getTitle());
                jsonEmbed.put("description", embed.getDescription());
                jsonEmbed.put("url", embed.getUrl());

                if (embed.getColor() != null) {
                    val color = embed.getColor();
                    var rgb = color.getRed();
                    rgb = (rgb << 8) + color.getGreen();
                    rgb = (rgb << 8) + color.getBlue();

                    jsonEmbed.put("color", rgb);
                }

                val footer = embed.getFooter();
                val image = embed.getImage();
                val thumbnail = embed.getThumbnail();
                val author = embed.getAuthor();
                val fields = embed.getFields();

                if (footer != null) {
                    val jsonFooter = new JSONObject();

                    jsonFooter.put("text", footer.getText());
                    jsonFooter.put("icon_url", footer.getIconUrl());
                    jsonEmbed.put("footer", jsonFooter);
                }

                if (image != null) {
                    val jsonImage = new JSONObject();

                    jsonImage.put("url", image.getUrl());
                    jsonEmbed.put("image", jsonImage);
                }

                if (thumbnail != null) {
                    val jsonThumbnail = new JSONObject();

                    jsonThumbnail.put("url", thumbnail.getUrl());
                    jsonEmbed.put("thumbnail", jsonThumbnail);
                }

                if (author != null) {
                    val jsonAuthor = new JSONObject();

                    jsonAuthor.put("name", author.getName());
                    jsonAuthor.put("url", author.getUrl());
                    jsonAuthor.put("icon_url", author.getIconUrl());
                    jsonEmbed.put("author", jsonAuthor);
                }

                List<JSONObject> jsonFields = new ArrayList<>();
                for (val field : fields) {
                    val jsonField = new JSONObject();

                    jsonField.put("name", field.getName());
                    jsonField.put("value", field.getValue());
                    jsonField.put("inline", field.isInline());

                    jsonFields.add(jsonField);
                }

                jsonEmbed.put("fields", jsonFields.toArray());
                embedObjects.add(jsonEmbed);
            }

            json.put("embeds", embedObjects.toArray());
        }

        try {
            String boundary = UUID.randomUUID().toString();
            val url = new URL(this.url);
            val connection = (HttpsURLConnection) url.openConnection();
            connection.addRequestProperty("User-Agent", "Java-DiscordWebhook-BY-Gelox_");
            connection.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            try (OutputStream output = connection.getOutputStream()) {
                writeJson(output, boundary, json.toString());

                int fileIndex = 1;
                for (File file : attachments) {
                    if (file != null && file.exists()) {
                        writeFile(output, boundary, file.getName() + fileIndex++, file);
                    } else {
                        throw new IllegalArgumentException("Attachment " + (file != null ? file.getName() : "[no filename found]") + " not found");
                    }
                }

                output.write(("--" + boundary + "--\r\n").getBytes());
                output.flush();
            }

            connection.getInputStream().close(); //I'm not sure why, but it doesn't work without getting the InputStream
            connection.disconnect();
        } catch (final @NonNull Exception e) {
            Logging.IMP.stacktrace(e, "Exception raised on discord webhook send process");
        }
    }

    private void writeJson(@NotNull OutputStream output, String boundary, String json) throws IOException {
        output.write(("--" + boundary + "\r\n").getBytes());
        output.write("Content-Disposition: form-data; name=\"payload_json\"\r\n\r\n".getBytes());
        output.write((json + "\r\n").getBytes());
    }

    private void writeFile(@NotNull OutputStream output, String boundary, String name, @NotNull File file) throws IOException {
        output.write(("--" + boundary + "\r\n").getBytes());
        output.write(("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + file.getName() + "\"\r\n").getBytes());
        output.write(("Content-Type: " + Files.probeContentType(file.toPath()) + "\r\n\r\n").getBytes());
        Files.copy(file.toPath(), output);
        output.write("\r\n".getBytes());
    }

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class EmbedObject {

        String title;
        String description;
        String url;
        Color color;

        Footer footer;
        Thumbnail thumbnail;
        Image image;
        Author author;

        final List<@NotNull Field> fields = new ArrayList<>();

        public EmbedObject setTitle(final String title) {
            this.title = title;
            return this;
        }

        public EmbedObject setDescription(final String description) {
            this.description = description;
            return this;
        }

        public EmbedObject setUrl(final String url) {
            this.url = url;
            return this;
        }

        public EmbedObject setColor(final Color color) {
            this.color = color;
            return this;
        }

        public EmbedObject setFooter(final String text, final String icon) {
            this.footer = new Footer(text, icon);
            return this;
        }

        public EmbedObject setThumbnail(final String url) {
            this.thumbnail = new Thumbnail(url);
            return this;
        }

        public EmbedObject setImage(final String url) {
            this.image = new Image(url);
            return this;
        }

        public EmbedObject setAuthor(final String name, final String url, final String icon) {
            this.author = new Author(name, url, icon);
            return this;
        }

        public EmbedObject addField(final String name, final String value, final boolean inline) {
            this.fields.add(new Field(name, value, inline));
            return this;
        }

        @FieldDefaults(level = AccessLevel.PRIVATE)
        @AllArgsConstructor
        @Getter
        private static class Footer {

            String text;
            String iconUrl;
        }

        @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
        @RequiredArgsConstructor
        @Getter
        private static class Thumbnail {
            String url;
        }


        @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
        @RequiredArgsConstructor
        @Getter
        private static class Image {
            String url;
        }


        @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
        @AllArgsConstructor
        @Getter
        private static class Author {

            String name;
            String url;
            String iconUrl;

        }


        @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
        @RequiredArgsConstructor
        @Getter
        private static class Field {
            String name;
            String value;
            boolean inline;
        }
    }


    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor
    @Getter
    private static class JSONObject {

        HashMap<String, Object> map = new HashMap<>();

        void put(final String key, final Object value) {
            if (value != null) {
                map.put(key, value);
            }
        }

        @Override
        public @NotNull String toString() {
            val builder = new StringBuilder();
            val entrySet = map.entrySet();
            builder.append("{");

            var i = 0;
            for (val entry : entrySet) {
                val val = entry.getValue();
                builder.append(quote(entry.getKey())).append(":");

                if (val instanceof String) {
                    builder.append(quote(String.valueOf(val)));
                } else if (val instanceof Integer) {
                    builder.append(Integer.valueOf(String.valueOf(val)));
                } else if (val instanceof Boolean) {
                    builder.append(val);
                } else if (val instanceof JSONObject) {
                    builder.append(val);
                } else if (val.getClass().isArray()) {
                    builder.append("[");
                    int len = Array.getLength(val);
                    for (int j = 0; j < len; j++) {
                        builder.append(Array.get(val, j).toString()).append(j != len - 1 ? "," : "");
                    }
                    builder.append("]");
                }

                builder.append(++i == entrySet.size() ? "}" : ",");
            }

            return builder.toString();
        }

        @Contract(pure = true)
        private @NotNull String quote(final String string) {
            return "\"" + string + "\"";
        }
    }
}