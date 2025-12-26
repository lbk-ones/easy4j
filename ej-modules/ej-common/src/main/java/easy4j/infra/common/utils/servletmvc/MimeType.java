package easy4j.infra.common.utils.servletmvc;

import lombok.Getter;

import java.util.Locale;
import java.util.Objects;

/**
 * 通用 Content-Type（MIME 类型）枚举类
 * 新增带 UTF-8 编码的完整 Content-Type 字段，区分字符型/二进制类型
 */
public enum MimeType {
    // ======================== 文本类（需 charset） ========================
    /** 纯文本 */
    TEXT_PLAIN("text/plain", "txt"),
    /** HTML 页面 */
    TEXT_HTML("text/html", "html,htm"),
    /** CSS 样式表 */
    TEXT_CSS("text/css", "css"),
    /** JavaScript 脚本 */
    TEXT_JAVASCRIPT("text/javascript", "js"),
    /** XML 文本 */
    TEXT_XML("text/xml", "xml"),
    /** CSV 表格 */
    TEXT_CSV("text/csv", "csv"),
    /** Markdown 文本 */
    TEXT_MARKDOWN("text/markdown", "md"),
    /** 富文本 */
    TEXT_RICHTEXT("text/richtext", "rtf"),
    /** 日历文本 */
    TEXT_CALENDAR("text/calendar", "ics"),

    // ======================== 图片类（二进制，无 charset） ========================
    /** JPEG 图片 */
    IMAGE_JPEG("image/jpeg", "jpg,jpeg,jpe"),
    /** PNG 图片 */
    IMAGE_PNG("image/png", "png"),
    /** GIF 图片 */
    IMAGE_GIF("image/gif", "gif"),
    /** BMP 图片 */
    IMAGE_BMP("image/bmp", "bmp"),
    /** WEBP 图片（高效压缩） */
    IMAGE_WEBP("image/webp", "webp"),
    /** SVG 矢量图（需 charset） */
    IMAGE_SVG("image/svg+xml", "svg"),
    /** TIFF 图片（高精度） */
    IMAGE_TIFF("image/tiff", "tiff,tif"),
    /** ICO 图标 */
    IMAGE_ICO("image/x-icon", "ico"),
    /** HEIC 图片（苹果格式） */
    IMAGE_HEIC("image/heic", "heic"),
    /** AVIF 图片（新一代压缩） */
    IMAGE_AVIF("image/avif", "avif"),

    // ======================== 音频类（二进制，无 charset） ========================
    /** MP3 音频 */
    AUDIO_MP3("audio/mpeg", "mp3"),
    /** WAV 音频（无损） */
    AUDIO_WAV("audio/wav", "wav"),
    /** M4A 音频 */
    AUDIO_M4A("audio/mp4", "m4a"),
    /** OGG 音频 */
    AUDIO_OGG("audio/ogg", "ogg"),
    /** FLAC 音频（无损） */
    AUDIO_FLAC("audio/flac", "flac"),
    /** AAC 音频 */
    AUDIO_AAC("audio/aac", "aac"),
    /** WMA 音频（Windows） */
    AUDIO_WMA("audio/x-ms-wma", "wma"),
    /** AMR 音频（语音） */
    AUDIO_AMR("audio/amr", "amr"),

    // ======================== 视频类（二进制，无 charset） ========================
    /** MP4 视频 */
    VIDEO_MP4("video/mp4", "mp4"),
    /** MOV 视频（QuickTime） */
    VIDEO_MOV("video/quicktime", "mov"),
    /** AVI 视频 */
    VIDEO_AVI("video/x-msvideo", "avi"),
    /** WEBM 视频 */
    VIDEO_WEBM("video/webm", "webm"),
    /** MKV 视频 */
    VIDEO_MKV("video/x-matroska", "mkv"),
    /** FLV 视频 */
    VIDEO_FLV("video/x-flv", "flv"),
    /** WMV 视频（Windows） */
    VIDEO_WMV("video/x-ms-wmv", "wmv"),
    /** MPEG 视频 */
    VIDEO_MPEG("video/mpeg", "mpeg,mpg"),
    /** 3GP 视频（移动端） */
    VIDEO_3GP("video/3gpp", "3gp"),

    // ======================== 应用类（部分需 charset） ========================
    /** JSON 数据（需 charset） */
    APPLICATION_JSON("application/json", "json"),
    /** JSON-LD 数据（需 charset） */
    APPLICATION_JSONLD("application/ld+json", "jsonld"),
    /** XML 应用类型（区别于文本XML，需 charset） */
    APPLICATION_XML("application/xml", "xml"),
    /** PDF 文档（二进制，无 charset） */
    APPLICATION_PDF("application/pdf", "pdf"),
    /** 二进制流（通用下载，无 charset） */
    APPLICATION_OCTET_STREAM("application/octet-stream", "bin,dat"),
    /** 表单（键值对，需 charset） */
    APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded", ""),
    /** 表单（文件上传，无 charset） */
    APPLICATION_MULTIPART_FORM_DATA("multipart/form-data", ""),
    /** ZIP 压缩包（二进制，无 charset） */
    APPLICATION_ZIP("application/zip", "zip"),
    /** GZIP 压缩包（二进制，无 charset） */
    APPLICATION_GZIP("application/gzip", "gz"),
    /** TAR 压缩包（二进制，无 charset） */
    APPLICATION_TAR("application/x-tar", "tar"),
    /** 7Z 压缩包（二进制，无 charset） */
    APPLICATION_7Z("application/x-7z-compressed", "7z"),
    /** RAR 压缩包（二进制，无 charset） */
    APPLICATION_RAR("application/x-rar-compressed", "rar"),
    /** JAR 包（二进制，无 charset） */
    APPLICATION_JAR("application/java-archive", "jar"),
    /** WAR 包（二进制，无 charset） */
    APPLICATION_WAR("application/java-web-archive", "war"),
    /** EAR 包（二进制，无 charset） */
    APPLICATION_EAR("application/java-ee-archive", "ear"),
    /** SQL 脚本（需 charset） */
    APPLICATION_SQL("application/sql", "sql"),
    /** YAML 配置（需 charset） */
    APPLICATION_YAML("application/yaml", "yaml,yml"),
    /** TOML 配置（需 charset） */
    APPLICATION_TOML("application/toml", "toml"),
    /** 二进制 JSON（无 charset） */
    APPLICATION_BSON("application/bson", "bson"),
    /** WebAssembly 二进制（无 charset） */
    APPLICATION_WASM("application/wasm", "wasm"),

    // ======================== 办公文档类（二进制，无 charset） ========================
    /** Word 文档（旧版 .doc） */
    APPLICATION_MSWORD("application/msword", "doc"),
    /** Word 文档（新版 .docx） */
    APPLICATION_VND_OPENXML_FORMATS_WORDPROCESSINGML_DOCUMENT("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx"),
    /** Excel 表格（旧版 .xls） */
    APPLICATION_VND_MS_EXCEL("application/vnd.ms-excel", "xls"),
    /** Excel 表格（新版 .xlsx） */
    APPLICATION_VND_OPENXML_FORMATS_SPREADSHEETML_SHEET("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"),
    /** PowerPoint 演示（旧版 .ppt） */
    APPLICATION_VND_MS_POWERPOINT("application/vnd.ms-powerpoint", "ppt"),
    /** PowerPoint 演示（新版 .pptx） */
    APPLICATION_VND_OPENXML_FORMATS_PRESENTATIONML_PRESENTATION("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx"),
    /** Visio 绘图 */
    APPLICATION_VND_MS_VISIO("application/vnd.visio", "vsd,vsdx"),
    /** OpenOffice 文本文档 */
    APPLICATION_VND_OASIS_OPENDOCUMENT_TEXT("application/vnd.oasis.opendocument.text", "odt"),
    /** OpenOffice 电子表格 */
    APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET("application/vnd.oasis.opendocument.spreadsheet", "ods"),

    // ======================== 字体类（二进制，无 charset） ========================
    /** TTF 字体 */
    FONT_TTF("font/ttf", "ttf"),
    /** OTF 字体 */
    FONT_OTF("font/otf", "otf"),
    /** WOFF 字体（Web 常用） */
    FONT_WOFF("font/woff", "woff"),
    /** WOFF2 字体（高效压缩） */
    FONT_WOFF2("font/woff2", "woff2"),
    /** EOT 字体（IE 专用） */
    FONT_EOT("application/vnd.ms-fontobject", "eot");

    /**
     * -- GETTER --
     *  获取基础 MIME 类型（无编码后缀）
     *
     * @return 如 "application/json"
     */
    // 基础 MIME 类型（无编码后缀，如 "application/json"）
    @Getter
    private final String baseMimeType;
    /**
     * -- GETTER --
     *  获取带 UTF-8 编码的完整 Content-Type（开发中最常用）
     *
     * @return 如 "application/json;charset=UTF-8"、"image/jpeg"（二进制类型无编码）
     */
    // 带 UTF-8 编码的完整 Content-Type（如 "application/json;charset=UTF-8"）
    @Getter
    private final String fullMimeTypeWithUtf8;
    // 关联的文件后缀（多个用逗号分隔）
    private final String extensions;

    /**
     * 构造方法：自动判断是否添加 charset=UTF-8 编码后缀
     * @param baseMimeType 基础 MIME 类型（如 "application/json"）
     * @param extensions 关联的文件后缀
     */
    MimeType(String baseMimeType, String extensions) {
        this.baseMimeType = baseMimeType;
        this.extensions = extensions == null ? "" : extensions;
        // 仅对「字符型类型」添加 charset=UTF-8，二进制类型直接使用基础 MIME
        this.fullMimeTypeWithUtf8 = isTextBasedType(baseMimeType) 
                ? baseMimeType + ";charset=UTF-8" 
                : baseMimeType;
    }

    /**
     * 判断是否为「字符型类型」（需要 charset 编码）
     * 规则：text/*、application/json*、application/xml*、image/svg+xml、application/sql、application/yaml、application/toml
     */
    private boolean isTextBasedType(String mimeType) {
        return mimeType.startsWith("text/")
                || mimeType.startsWith("application/json")
                || mimeType.startsWith("application/xml")
                || mimeType.equals("image/svg+xml")
                || mimeType.equals("application/sql")
                || mimeType.equals("application/yaml")
                || mimeType.equals("application/toml");
    }

    // ======================== 核心获取方法 ========================

    /**
     * 获取关联的文件后缀
     * @return 后缀数组（如 ["jpg", "jpeg"]）
     */
    public String[] getExtensions() {
        if (extensions.isEmpty()) {
            return new String[0];
        }
        return extensions.split(",");
    }

    // ======================== 工具方法 ========================

    /**
     * 根据文件后缀获取对应的 ContentType 枚举
     * @param extension 文件后缀（如 "jpg"、"JSON"，不区分大小写）
     * @return 匹配的枚举，无匹配则返回 APPLICATION_OCTET_STREAM
     */
    public static MimeType fromFileExtension(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            return APPLICATION_OCTET_STREAM;
        }
        String lowerExt = extension.trim().toLowerCase(Locale.ENGLISH);
        if (lowerExt.startsWith(".")) {
            lowerExt = lowerExt.substring(1);
        }

        for (MimeType type : values()) {
            for (String ext : type.getExtensions()) {
                if (Objects.equals(lowerExt, ext)) {
                    return type;
                }
            }
        }
        return APPLICATION_OCTET_STREAM;
    }

    /**
     * 根据文件名获取 ContentType 枚举
     * @param fileName 文件名（如 "test.jpg"、"data.json"）
     * @return 匹配的枚举，无匹配则返回 APPLICATION_OCTET_STREAM
     */
    public static MimeType fromFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return APPLICATION_OCTET_STREAM;
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return APPLICATION_OCTET_STREAM;
        }
        String extension = fileName.substring(lastDotIndex + 1);
        return fromFileExtension(extension);
    }

    /**
     * 根据 MIME 类型字符串反向查找枚举（兼容带/不带编码后缀）
     * @param mimeType Content-Type 字符串（如 "application/json" 或 "application/json;charset=UTF-8"）
     * @return 匹配的枚举，无匹配则返回 null
     */
    public static MimeType fromMimeType(String mimeType) {
        if (mimeType == null || mimeType.trim().isEmpty()) {
            return null;
        }
        // 去除编码后缀，只保留基础 MIME 部分（如 "application/json;charset=UTF-8" → "application/json"）
        String baseMime = mimeType.trim().toLowerCase(Locale.ENGLISH)
                .split(";")[0]
                .trim();
        for (MimeType type : values()) {
            if (type.baseMimeType.equals(baseMime)) {
                return type;
            }
        }
        return null;
    }
}