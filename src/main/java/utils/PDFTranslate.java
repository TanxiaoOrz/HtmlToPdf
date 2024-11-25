package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: 张骏山
 * @Date: 2024/11/18 14:32
 * @PackageName: utils
 * @ClassName: PDFTranslate
 * @Description: PDF转换执行类
 * @Version: 1.0
 **/
public class PDFTranslate {

    public static String WK_PDF_TO_HTML_ROUTE = null;

    private FileUtilsInterface fileUtils;

    private String aimRoute;
    private String sourceFileRoute;
    private List<String> sourceFolderRoutes;

    public PDFTranslate(String aimRoute, List<String> sourceFolderRoutes) {
        this.aimRoute = aimRoute;
        this.sourceFolderRoutes = sourceFolderRoutes;
        fileUtils = new BasicFileUtils();
        check();
    }

    public PDFTranslate(String aimRoute, String sourceFileRoute) {
        this.aimRoute = aimRoute;
        this.sourceFileRoute = sourceFileRoute;
        fileUtils = new BasicFileUtils();
        check();
    }

    public PDFTranslate(FileUtilsInterface fileUtils, String aimRoute, String sourceFileRoute) {
        this.fileUtils = fileUtils;
        this.aimRoute = aimRoute;
        this.sourceFileRoute = sourceFileRoute;
        check();
    }

    public PDFTranslate(FileUtilsInterface fileUtils, String aimRoute, List<String> sourceFolderRoutes) {
        this.fileUtils = fileUtils;
        this.aimRoute = aimRoute;
        this.sourceFolderRoutes = sourceFolderRoutes;
        check();
    }

    public PDFTranslate(String sourceFileRoute) {
        this.sourceFileRoute = sourceFileRoute;
    }

    public PDFTranslate(List<String> sourceFolderRoutes) {
        this.sourceFolderRoutes = sourceFolderRoutes;
    }

    private void check() {
        String suffix = File.separator;
        if (!aimRoute.endsWith(suffix))
            aimRoute = aimRoute += suffix;
    }

    public int convert(String args) throws Exception {
        if (WK_PDF_TO_HTML_ROUTE == null) {
            throw new Exception("缺少WKHTMLTOPDF的安装路径");
        }
        if (aimRoute == null) {
            aimRoute = Paths.get("").toAbsolutePath().toString();
        }
        if (sourceFolderRoutes == null) {
            return convertSingle(args);
        }
        AtomicInteger con = new AtomicInteger();
        sourceFolderRoutes.stream().forEach(route -> {
            sourceFileRoute = route;
            try {
                con.addAndGet(convertSingle(args));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        return con.get();

    }

    public int convertSingle(String args) throws Exception {
        if (WK_PDF_TO_HTML_ROUTE == null) {
            throw new Exception("缺少WKHTMLTOPDF的安装路径");
        }
        if (sourceFileRoute == null) {
            System.out.println("Fail with null ");
            return 0;
        }
        try {
            String command = WK_PDF_TO_HTML_ROUTE + (args == null ? " " : args) +
                    sourceFileRoute + " " + aimRoute  +
                    fileUtils.getAimFileName(
                            sourceFileRoute.substring(sourceFileRoute.lastIndexOf(File.separator) + 1)
                    );

            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.out.println(command + ": Fail with code " + exitCode);
                return 0;
            } else {
                System.out.println(command + ": Success");
                return 1;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println(sourceFileRoute + ": Fail with exception " + e.getMessage());
            return 0;
        }
    }


}
