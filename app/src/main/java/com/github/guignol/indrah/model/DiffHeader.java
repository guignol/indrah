package com.github.guignol.indrah.model;

import com.github.guignol.indrah.utils.ListUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * diff --git a/file1 b/file2
 * <p/>
 * old mode <mode>
 * new mode <mode>
 * deleted file mode <mode>
 * new file mode <mode>
 * copy from <path>
 * copy to <path>
 * rename from <path>
 * rename to <path>
 * similarity index <number>
 * dissimilarity index <number>
 * index <hash>..<hash> <mode>
 * <p>
 * https://git-scm.com/docs/git-diff-index#_combined_diff_format
 * --------this example shows a merge with two parents
 * index <hash>,<hash>..<hash>
 * mode <mode>,<mode>..<mode>
 * new file mode <mode>
 * deleted file mode <mode>,<mode>
 */
public class DiffHeader {

    public static class Prefix {
        public static final String NEW_FILE_MODE = "new file mode";
        public static final String DELETED = "deleted";
        public static final String RENAME = "rename";
        public static final String INDEX = "index";
        public static final String MINUS_DEV_NULL = "--- /dev/null";
        public static final String MINUS_FILE_PATH = "--- a/";
        public static final String PLUS_FILE_PATH = "+++ b/";
    }

    @Nullable
    static String hashBefore(List<String> headerLines) {
        final String hashBefore = hashBeforeAfter(headerLines, 0);
        if (hashBefore != null && hashBefore.split(",").length == 1) {
            return hashBefore;
        } else {
            // TODO 親が1つ以上のマージは未対応
            return null;
        }
    }

    @Nullable
    static String hashAfter(List<String> headerLines) {
        return hashBeforeAfter(headerLines, 1);
    }

    @Nullable
    private static String hashBeforeAfter(List<String> headerLines, int index) {
//        headerLines.forEach(System.out::println);
        final String indexLine = ListUtils.find(headerLines, line -> line.startsWith(Prefix.INDEX));
        if (indexLine == null) {
            return null;
        } else {
            return indexLine.split(" ")[1].split("\\.\\.")[index];
        }
    }

    // TODO 例えば新規、リネーム、削除の判定
}
