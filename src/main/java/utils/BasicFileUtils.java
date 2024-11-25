package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: 张骏山
 * @Date: 2024/11/24 15:30
 * @PackageName: utils
 * @ClassName: BasicFileUtils
 * @Description: 基础文件工具常用类
 * @Version: 1.0
 */
public class BasicFileUtils implements FileUtilsInterface{
    @Override
    public List<String> getFileRoutes(String fileFolder) throws Exception {
        File dir = new File(fileFolder);
        if (!dir.exists())
            throw new Exception("不存在的目录");
        ArrayList<String> files = new ArrayList<>();
        File[] listFiles = dir.listFiles();

        if (listFiles != null) {
            Arrays.stream(listFiles).forEach(file -> files.add(file.getAbsolutePath()));
        }

        return files;
    }

    @Override
    public String getAimFileName(String filename) {
        String[] split = filename.split("\\.");
        StringBuilder aimName = new StringBuilder();
        for (int i = 0; i < split.length -1; i++) {
            aimName.append(split[i]);
        }
        aimName.append(".pdf");
        return aimName.toString();
    }
}
