package com.github.guignol.indrah.command;

import java.util.stream.Stream;

class CommandOptions {

    static Stream<String> diffStandard() {
        return Stream.concat(quotePath(false), mnemonicPrefix(false));

    }

    static Stream<String> quotePath(boolean quote) {
        // -c core.quotepath=false
        final String value = quote ? "true" : "false";
        return Stream.of("-c", "core.quotepath=" + value);
    }

    /**
     * git diff が「a/」「b/」の代わりに、「i」「/w」などを使うようになる
     * https://git-scm.com/docs/diff-config#diff-config-diffmnemonicPrefix
     */
    static Stream<String> mnemonicPrefix(boolean mnemonicPrefix) {
        // -c diff.mnemonicprefix=false
        final String value = mnemonicPrefix ? "true" : "false";
        return Stream.of("-c", "diff.mnemonicprefix=" + value);
    }
}
