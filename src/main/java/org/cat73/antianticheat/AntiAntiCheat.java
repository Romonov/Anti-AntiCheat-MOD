package org.cat73.antianticheat;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.cat73.antianticheat.bukkit.handle.AntiCheatHandle;
import org.cat73.antianticheat.network.PacketHandler;
import org.cat73.antianticheat.util.MD5Util;

import net.minecraft.util.StringUtils;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = AntiAntiCheat.MODID, version = AntiAntiCheat.VERSION)
public class AntiAntiCheat {
    public static final String MODID = "AntiCheat";
    public static final String VERSION = "1.0";

    public static EnumMap<Side, FMLEmbeddedChannel> channelmap;
    public static final PacketHandler packetHandler = new PacketHandler();
    private Configuration config;
    private String selfMd5 = "b004abfb2a019a6563b51bfae6456a92";

    private static AntiAntiCheat instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;
        this.config = new Configuration(new File(String.format("%s/%s.cfg", event.getModConfigurationDirectory(), "AntiAntiCheat")));

        channelmap = NetworkRegistry.INSTANCE.newChannel("LALAACM", new AntiCheatHandle());
        packetHandler.perInitialise();

        FMLLog.info("preInit over");
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        packetHandler.Initialise();

        FMLLog.info("Init over");
    }

    public String getMD5String() {
        // 获取MOD的MD5列表
        HashMap<String, HashMap<String, String>> mods = getMods();
        String[] md5List = new String[mods.size()];
        
        // 将MD5列表整理成配置文件
        int i = 0;
        for(String modid : mods.keySet()) {
            HashMap<String, String> info = mods.get(modid);
            String md5 = info.get("md5");
            String name = info.get("name");
            String filename = info.get("filename");
            if(modid.equalsIgnoreCase(MODID)) {
                md5 = this.selfMd5;
            }
            md5List[i++] = md5 + " ---> " + modid + " ---> " + name + " ---> " + filename;
        }

        // 保存配置
        this.config.load();
        md5List = this.config.get("ClientConfig", "MD5List", md5List, "MD5 list").getStringList();
        this.config.save();

        // 将配置文件中的MD5列表处理成字符串
        String md5s = "";
        for (String md5 : md5List) {
            String[] tmp = md5.split(" ---> ");
            if(tmp.length >= 1) {
                md5s = md5s + tmp[0] + ",";
            }
        }
        if (!StringUtils.isNullOrEmpty(md5s)) {
            md5s = md5s.substring(0, md5s.length() - 1);
        }
        
        // 返回MD5字符串
        return md5s;
    }

    private HashMap<String, HashMap<String, String>> getMods() {
        HashMap<String, HashMap<String, String>> modMap = new HashMap<String, HashMap<String, String>>();
        MD5Util md5Util = new MD5Util();
        try {
            Class<?> minecraftClass = Class.forName("net.minecraft.client.main.Main");

            String path = minecraftClass.getProtectionDomain().getCodeSource().getLocation().getPath().split("!")[0];
            path = URLDecoder.decode(path, "UTF-8");
            path = path.substring(6);
            FMLLog.info("ClassPath---%s", path);
            File jarFile = new File(path);
            String md5 = md5Util.getMd5(jarFile);
            FMLLog.info("jar---%s:%s", jarFile.getName(), md5);
            packModInfo(modMap, jarFile.getName(), jarFile.getName(), md5, jarFile.getName());
        } catch (ClassNotFoundException e) {
            FMLLog.log(Level.ERROR, "%s:%s", "GameJarERROR", e);
        } catch (NoSuchAlgorithmException e) {
            FMLLog.log(Level.ERROR, "%s:%s", "GameJarERROR", e);
        } catch (IOException e) {
            FMLLog.log(Level.ERROR, "%s:%s", "GameJarERROR", e);
        }

        List<ModContainer> mods = Loader.instance().getModList();
        for (ModContainer mod : mods) {
            String name = mod.getName();
            if ((name.equals("Forge Mod Loader")) || (name.equals("Minecraft Forge")) || (name.equals("Minecraft Coder Pack"))) {
                continue;
            }

            File modFile = mod.getSource();
            try {
                String modMD5 = md5Util.getMd5(modFile);
                FMLLog.info("modjar----%s:%s", mod.getName(), modMD5);
                packModInfo(modMap, name, mod.getModId(), modMD5, modFile.getName());
            } catch (NoSuchAlgorithmException e) {
                FMLLog.log(Level.ERROR, "%s:%s", mod.getName(), e);
                continue;
            } catch (IOException e) {
                FMLLog.log(Level.ERROR, "%s:%s", mod.getName(), e);
                md5Util.researchfile(modFile, false);
            }
        }
        return modMap;
    }

    private void packModInfo(HashMap<String, HashMap<String, String>> modMap, String name, String modid, String md5, String filename) {
        HashMap<String, String> info = new HashMap<String, String>();
        info.put("name", name);
        info.put("md5", md5);
        info.put("filename", filename);
        modMap.put(modid, info);
    }

    public static AntiAntiCheat getInstance() {
        return instance;
    }
}
