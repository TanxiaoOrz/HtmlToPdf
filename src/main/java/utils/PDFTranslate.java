package utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
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

    public static String WK_PDF_TO_HTML_ROUTE = "";

    private String aimRoute;
    private String sourceFileRoute;
    private String[] sourceFolderRoutes;

    public PDFTranslate(String aimRoute, String[] sourceFolderRoutes) {
        this.aimRoute = aimRoute;
        this.sourceFolderRoutes = sourceFolderRoutes;
    }

    public PDFTranslate(String aimRoute, String sourceFileRoute) {
        this.aimRoute = aimRoute;
        this.sourceFileRoute = sourceFileRoute;
    }

    public PDFTranslate(String sourceFileRoute) {
        this.sourceFileRoute = sourceFileRoute;
    }

    public PDFTranslate(String[] sourceFolderRoutes) {
        this.sourceFolderRoutes = sourceFolderRoutes;
    }

    public int convert() throws Exception {
        if (WK_PDF_TO_HTML_ROUTE == null) {
            throw new Exception("缺少WKHTMLTOPDF的安装路径");
        }
        if (aimRoute == null) {
            aimRoute = Paths.get("").toAbsolutePath().toString();
        }
        if (sourceFolderRoutes == null) {
            return convertSingle();
        }
        AtomicInteger con = new AtomicInteger();
        Arrays.stream(sourceFolderRoutes).forEach(route -> {
            sourceFileRoute = route;
            try {
                con.addAndGet(convertSingle());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        return con.get();

    }

    public int convertSingle() throws Exception {
        if (WK_PDF_TO_HTML_ROUTE == null) {
            throw new Exception("缺少WKHTMLTOPDF的安装路径");
        }
        if (sourceFileRoute == null) {
            System.out.println("Fail with null " );
            return 0;
        }
        try {
            String command = WK_PDF_TO_HTML_ROUTE + " " + sourceFileRoute + " " + aimRoute;

            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.out.println(sourceFileRoute+ ": Fail with code " + exitCode);
                return 0;
            } else {
                System.out.println(sourceFileRoute+ ": Success" );
                return 1;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println(sourceFileRoute+ ": Fail with exception " + e.getMessage());
            return 0;
        }
    }


}
