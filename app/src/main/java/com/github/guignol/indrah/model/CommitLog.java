package com.github.guignol.indrah.model;

import com.github.guignol.indrah.utils.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommitLog {

    public final String commit;
    public final String message;
    private final List<String> parents;
    private String newMessage = null;

    public CommitLog(String commit, String message, List<String> parents) {
        this.commit = commit;
        this.message = message;
        this.parents = parents;
    }

    public void print() {
        System.out.println("commit: " + commit);
        System.out.println("parents: " + String.join(", ", parents));
        System.out.println("message: " + message);
    }

    public void editMessage(String newMessage) {
        if (message.equals(newMessage)
                || newMessage == null
                || newMessage.trim().isEmpty()
                // TODO 先頭までトリムしてしまうのは間違ってるが、とりあえず
                || newMessage.trim().equals(message.trim())) {
            this.newMessage = null;
        } else {
            this.newMessage = newMessage;
        }
    }

    @Nullable
    public String getNewMessage() {
        return newMessage;
    }

    public boolean hasNewMessage() {
        return !StringUtils.isBlank(newMessage);
    }

    public boolean isMergeCommit() {
        return 1 < parents.size();
    }

    public static boolean hasMergeCommit(List<CommitLog> targets) {
        for (CommitLog log : targets) {
            if (log.isMergeCommit()) {
                return true;
            }
        }
        return false;
    }
}
