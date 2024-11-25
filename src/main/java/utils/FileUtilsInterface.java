package utils;

import java.util.List;

/**
 * @Author: 张骏山
 * @Date: 2024/11/18 14:32
 * @PackageName: utils
 * @ClassName: FileUtilsInterface
 * @Description: 文件工具常用类类
 * @Version: 1.0
 **/
public interface FileUtilsInterface {

    List<String> getFileRoutes(String fileFolder) throws Exception;

    String getAimFileName(String filename);

}
