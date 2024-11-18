package utils;

/**
 * @Author: 张骏山
 * @Date: 2024/11/18 14:32
 * @PackageName: utils
 * @ClassName: GetFiles
 * @Description: 文件范围获取类
 * @Version: 1.0
 **/
public interface GetFiles {

    String[] getFileRoutes(String fileFolder);

    String getAimFileName(String filename);

}
