//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.ccbluex.liquidbounce.memoryfix;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IChatComponent.Serializer;
import net.minecraftforge.common.ForgeVersion;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

public class UpdateChecker extends Thread {
    private final String url;
    private final Consumer<UpdateResponse> callback;
    private final Gson gson;

    public UpdateChecker(String url, Consumer<UpdateResponse> callback) {
        this.gson = (new GsonBuilder()).registerTypeHierarchyAdapter(IChatComponent.class, new Serializer()).registerTypeHierarchyAdapter(ChatStyle.class, new ChatStyle.Serializer()).registerTypeAdapterFactory(new EnumTypeAdapterFactory()).setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        this.url = url;
        this.callback = callback;
    }

    public void run() {
        int retry = 0;

        while(retry < 3) {
            try {
                UpdateResponse response = this.check(this.url);

                try {
                    this.callback.accept(response);
                } catch (Exception var5) {
                    var5.printStackTrace();
                }

                return;
            } catch (Exception var6) {
                System.out.println("GET " + this.url + " failed:");
                System.out.println(var6.toString());

                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException var4) {
                    return;
                }

                ++retry;
            }
        }

    }

    private UpdateResponse check(String url) throws IOException {
        HttpURLConnection con = null;

        Object var7;
        try {
            con = (HttpURLConnection)(new URL(url)).openConnection();
            con.setRequestMethod("GET");
            String agent = "Java/" + System.getProperty("java.version") + " " + "Forge/" + ForgeVersion.getVersion() + " " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + " ";
            con.setRequestProperty("User-Agent", agent);
            int response = con.getResponseCode();
            if (response != 200) {
                throw new IOException("HTTP " + response);
            }

            InputStreamReader in = new InputStreamReader(con.getInputStream(), "UTF-8");
            Throwable var6 = null;

            try {
                var7 = (UpdateResponse)this.gson.fromJson(in, UpdateResponse.class);
            } catch (Throwable var23) {
                var7 = var23;
                var6 = var23;
                throw var23;
            } finally {
                if (in != null) {
                    if (var6 != null) {
                        try {
                            in.close();
                        } catch (Throwable var22) {
                            var6.addSuppressed(var22);
                        }
                    } else {
                        in.close();
                    }
                }

            }
        } finally {
            if (con != null) {
                con.disconnect();
            }

        }

        return (UpdateResponse)var7;
    }

    public static class UpdateResponse {
        private final IChatComponent updateMessage;

        public UpdateResponse(IChatComponent updateMessage) {
            this.updateMessage = updateMessage;
        }

        public IChatComponent getUpdateMessage() {
            return this.updateMessage;
        }
    }
}
