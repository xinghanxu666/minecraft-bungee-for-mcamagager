package com.tx09;

import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

public class tx09 extends Plugin implements Listener {
    private Configuration config;
    private int onlinePlayers = 0; // 在线玩家计数器

    @Override
    public void onEnable() {
        getLogger().info("插件已启用！作者QQ群:664085606");
        getProxy().getPluginManager().registerListener(this, this);

        // 加载配置文件
        loadConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("插件已关闭！感谢使用！");
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent event) {
        onlinePlayers++; // 玩家加入，计数器加1

        if (onlinePlayers == 1) {
            // 第一个玩家加入时发送请求
            sendRequest();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerDisconnectEvent event) {
        onlinePlayers--; // 玩家退出，计数器减1
    }

    private void loadConfig() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdir();
            }

            File configFile = new File(getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                InputStream resourceAsStream = getResourceAsStream("config.yml");
                Files.copy(resourceAsStream, configFile.toPath());
            }

            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRequest() {
        // 获取配置项的值并在需要时使用
        String apiUrl = config.getString("apiUrl");
        String uuid = config.getString("uuid");
        String remoteUuid = config.getString("remoteUuid");
        String apiKey = config.getString("apikey");

        // 构建API请求URL
        String fullUrl = apiUrl + "?uuid=" + uuid + "&remote_uuid=" + remoteUuid + "&apikey=" + apiKey;

        try {
            // 创建URL对象
            URL url = new URL(fullUrl);

            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // 获取响应
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 读取响应内容
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // 在这里处理API响应
                String apiResponse = response.toString();
                getLogger().info("API响应: " + apiResponse);

                // 向控制台输出消息
                getLogger().info("已向指定服务器发送启动指令！");
            } else {
                getLogger().warning("API请求失败，响应代码：" + responseCode);
            }

            // 关闭连接
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
