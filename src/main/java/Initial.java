import utils.BasicFileUtils;
import utils.PDFTranslate;

import java.util.List;

/**
 * @Author: 张骏山
 * @Date: 2024/11/18 14:27
 * @PackageName: PACKAGE_NAME
 * @ClassName: Initial
 * @Description: 启动类
 * @Version: 1.0
 **/
public class Initial {
    static String DEFAULT_HTML_ROUTE = "";

    static String DEFAULT_PDF_ROUTE = "";

    public static void main(String[] args) {
        // 直接写可执行文件的真实路径即可，不需要也不应该手动加引号：
        // 引号会被当成文件名的一部分。（PDFTranslate 内部也会兜底去掉多余的引号）
        PDFTranslate.WK_PDF_TO_HTML_ROUTE = "D:\\Program Files\\wkhtmltopdf\\bin\\wkhtmltopdf.exe";
        BasicFileUtils basicFileUtils = new BasicFileUtils();
        try {
            List<String> fileRoutes = basicFileUtils.getFileRoutes("D:\\改造适应pdf-html\\");
            new PDFTranslate(basicFileUtils,
                    "D:\\改造适应pdf-pdf-test",
                    fileRoutes
                    )
                    .convert(" --page-height 800mm --page-width 297mm ");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
