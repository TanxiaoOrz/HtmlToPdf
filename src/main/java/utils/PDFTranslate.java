package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
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
        if (aimRoute != null && !aimRoute.endsWith(File.separator))
            aimRoute = aimRoute + File.separator;
    }

    public int convert(String args) throws Exception {
        if (WK_PDF_TO_HTML_ROUTE == null) {
            throw new Exception("缺少WKHTMLTOPDF的安装路径");
        }
        if (aimRoute == null) {
            aimRoute = Paths.get("").toAbsolutePath().toString() + File.separator;
        }
        if (sourceFolderRoutes == null) {
            return convertSingle(args);
        }
        AtomicInteger con = new AtomicInteger();
        int total = sourceFolderRoutes.size();
        for (String route : sourceFolderRoutes) {
            sourceFileRoute = route;
            con.addAndGet(convertSingle(args));
            System.out.println("[进度] " + con.get() + "/" + total + " 成功");
        }

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

        File executable = resolveExecutable(WK_PDF_TO_HTML_ROUTE);
        if (executable == null) {
            System.out.println(sourceFileRoute + ": Fail with 找不到可执行文件 " + WK_PDF_TO_HTML_ROUTE);
            return 0;
        }

        // wkhtmltopdf 不会自动创建输出目录，目录不存在时会报
        // "Unable to write to destination" 并以 exit=1 结束，所以这里必须先建目录。
        File aimFile = new File(buildAimFilePath());
        File aimFolder = aimFile.getParentFile();
        if (aimFolder != null && !aimFolder.exists() && !aimFolder.mkdirs()) {
            System.out.println(sourceFileRoute + ": Fail with 无法创建输出目录 " + aimFolder.getAbsolutePath());
            return 0;
        }

        // 关键：用参数列表而不是拼成一整行字符串，
        // 否则 Runtime.exec(String) 会按空白切分，含空格的路径（如 D:\Program Files）会被切断。
        List<String> command = new ArrayList<>();
        command.add(executable.getAbsolutePath());
        command.addAll(splitArguments(args));
        command.add(sourceFileRoute);
        command.add(aimFile.getAbsolutePath());

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            // 读走子进程输出，避免缓冲区写满导致 waitFor 一直阻塞
            drain(process);
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.out.println(joinForLog(command) + ": Fail with code " + exitCode);
                return 0;
            } else if (!aimFile.isFile() || aimFile.length() == 0) {
                // 进程 exit=0 但文件没落地（输出目录不可写等），不能算成功
                System.out.println(joinForLog(command) + ": Fail with 未生成有效文件 " + aimFile.getAbsolutePath());
                return 0;
            } else {
                System.out.println(joinForLog(command) + ": Success -> " + aimFile.getAbsolutePath());
                return 1;
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            e.printStackTrace();
            System.out.println(sourceFileRoute + ": Fail with exception " + e.getMessage());
            return 0;
        }
    }

    /**
     * 解析可执行文件路径：去掉用户可能额外包上的单/双引号，缺 .exe 时补齐。
     */
    private File resolveExecutable(String route) {
        String path = route.trim();
        if (path.length() >= 2
                && ((path.startsWith("'") && path.endsWith("'"))
                || (path.startsWith("\"") && path.endsWith("\"")))) {
            path = path.substring(1, path.length() - 1).trim();
        }
        File file = new File(path);
        if (file.isFile()) {
            return file;
        }
        if (!path.toLowerCase().endsWith(".exe")) {
            File withExe = new File(path + ".exe");
            if (withExe.isFile()) {
                return withExe;
            }
        }
        return file.isFile() ? file : null;
    }

    private String buildAimFilePath() {
        String sourceName = new File(sourceFileRoute).getName();
        String aimName = fileUtils == null
                ? new BasicFileUtils().getAimFileName(sourceName)
                : fileUtils.getAimFileName(sourceName);
        return aimRoute.endsWith(File.separator) ? aimRoute + aimName : aimRoute + File.separator + aimName;
    }

    /**
     * 按空白切分参数串，支持用单/双引号包裹含空格的单个参数。
     */
    static List<String> splitArguments(String raw) {
        List<String> tokens = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) {
            return tokens;
        }
        StringBuilder current = new StringBuilder();
        char quote = 0;
        boolean started = false;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (quote != 0) {
                if (c == quote) {
                    quote = 0;
                } else {
                    current.append(c);
                }
            } else if (c == '"' || c == '\'') {
                quote = c;
                started = true;
            } else if (Character.isWhitespace(c)) {
                if (started || current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                    started = false;
                }
            } else {
                current.append(c);
                started = true;
            }
        }
        if (started || current.length() > 0) {
            tokens.add(current.toString());
        }
        return tokens;
    }

    private void drain(Process process) {
        Charset charset = Charset.defaultCharset();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[" + new File(sourceFileRoute).getName() + "] " + line);
            }
        } catch (IOException ignored) {
            // 输出读取失败不影响转换结果判定
        }
    }

    private String joinForLog(List<String> command) {
        StringBuilder sb = new StringBuilder();
        for (String token : command) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(token.indexOf(' ') >= 0 ? "\"" + token + "\"" : token);
        }
        return sb.toString();
    }
}
