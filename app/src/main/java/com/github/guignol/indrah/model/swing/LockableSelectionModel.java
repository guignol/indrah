package com.github.guignol.indrah.model.swing;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class LockableSelectionModel extends DefaultListSelectionModel {

    @Nullable
    private Runnable onLocked;
    @Nullable
    private Runnable onUnlocked;
    private final AtomicBoolean locked = new AtomicBoolean(false);

    public LockableSelectionModel onLocked(Runnable onLocked) {
        this.onLocked = onLocked;
        return this;
    }

    public LockableSelectionModel onUnlocked(Runnable onUnlocked) {
        this.onUnlocked = onUnlocked;
        return this;
    }

    public boolean isLocked() {
        return locked.get();
    }

    public void lock() {
        if (locked.getAndSet(true)) {
            return;
        }
        if (onLocked != null) {
            onLocked.run();
        }
    }

    public void unlock() {
        if (!locked.getAndSet(false)) {
            return;
        }
        if (onUnlocked != null) {
            onUnlocked.run();
        }
    }

    @Override
    public void addSelectionInterval(int index0, int index1) {
        if (locked.get()) {
            logging(toString() + ": cancel because locked: " + "addSelectionInterval");
            return;
        }
        logging(toString() + ": change selection: " + "addSelectionInterval");
        super.addSelectionInterval(index0, index1);
    }

    @Override
    public void setSelectionInterval(int index0, int index1) {
        if (locked.get()) {
            logging(toString() + ": cancel because locked: " + "setSelectionInterval");
            return;
        }
        logging(toString() + ": change selection: " + "setSelectionInterval");
        super.setSelectionInterval(index0, index1);
    }

    @Override
    public void clearSelection() {
        if (locked.get()) {
            logging(toString() + ": cancel because locked: " + "clearSelection");
            return;
        }
        logging(toString() + ": change selection: " + "clearSelection");
        super.clearSelection();
    }

    @Override
    public void removeSelectionInterval(int index0, int index1) {
        if (locked.get()) {
            logging(toString() + ": cancel because locked: " + "removeSelectionInterval");
            return;
        }
        logging(toString() + ": change selection: " + "removeSelectionInterval");
        super.removeSelectionInterval(index0, index1);
    }

    @Override
    public void insertIndexInterval(int index, int length, boolean before) {
        if (locked.get()) {
            logging(toString() + ": cancel because locked: " + "insertIndexInterval");
            return;
        }
        logging(toString() + ": change selection: " + "insertIndexInterval");
        super.insertIndexInterval(index, length, before);
    }

    @Override
    public void removeIndexInterval(int index0, int index1) {
        if (locked.get()) {
            logging(toString() + ": cancel because locked: " + "removeIndexInterval");
            return;
        }
        logging(toString() + ": change selection: " + "removeIndexInterval");
        super.removeIndexInterval(index0, index1);
    }

    @Override
    public void setAnchorSelectionIndex(int anchorIndex) {
        if (locked.get()) {
            logging(toString() + ": cancel because locked: " + "setAnchorSelectionIndex");
            return;
        }
        logging(toString() + ": change selection: " + "setAnchorSelectionIndex");
        super.setAnchorSelectionIndex(anchorIndex);
    }

    @Override
    public void moveLeadSelectionIndex(int leadIndex) {
        if (locked.get()) {
            logging(toString() + ": cancel because locked: " + "moveLeadSelectionIndex");
            return;
        }
        logging(toString() + ": change selection: " + "moveLeadSelectionIndex");
        super.moveLeadSelectionIndex(leadIndex);
    }

    @Override
    public void setLeadSelectionIndex(int leadIndex) {
        if (locked.get()) {
            logging(toString() + ": cancel because locked: " + "setLeadSelectionIndex");
            return;
        }
        logging(toString() + ": change selection: " + "setLeadSelectionIndex");
        super.setLeadSelectionIndex(leadIndex);
    }

    static void logging(String message) {
//        System.out.println(message);
    }
}
