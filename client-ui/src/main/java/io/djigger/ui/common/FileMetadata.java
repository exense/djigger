package io.djigger.ui.common;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public class FileMetadata {
    public final String loadDialogTitle;
    public final String saveDialogTitle;
    public final FileFilter[] preferredFilters;

    public FileMetadata(String loadTitle, String saveTitle, FileFilter... preferredFilters) {
        this.loadDialogTitle = loadTitle;
        this.saveDialogTitle = saveTitle;
        this.preferredFilters = preferredFilters;
    }

    public static final FileMetadata SESSION = new FileMetadata(
        "Load Session", "Save Session",
        new ExtensionSuggestingFileNameFilter("djigger session (*.djs)", "djs")
    );

    public static final FileMetadata SESSIONLIST = new FileMetadata(
        "Load Session list", "Save Session list",
        new ExtensionSuggestingFileNameFilter("djigger session list (*.djl)", "djl")
    );


    public static final FileMetadata SUBSCRIPTIONS = new FileMetadata(
        "Load Subscriptions", "Save Subscriptions",
        new ExtensionSuggestingFileNameFilter("djigger subscriptions (*.djr)", "djr")
    );

    public static final FileMetadata LEGEND = new FileMetadata(
        "Load Legend", "Save Legend",
        new ExtensionSuggestingFileNameFilter("djigger block legend (*.djb)", "djb")
    );

    public static final FileMetadata JSTACK = new FileMetadata(
        "Load jstack", "Save jstack",
        new FileNameExtensionFilter("common jstack file names (*.out *.log *.txt *.jstack)", "out", "txt", "log", "jstack")
    );

    public static final FileMetadata TOOLS_JAR = new FileMetadata(
        "Select tools.jar", "--unneeded--", new FileFilter() {
        @Override
        public boolean accept(File f) {
            if (f != null) {
                return f.isDirectory() || f.getName().equalsIgnoreCase("tools.jar");
            }
            return false;
        }

        @Override
        public String getDescription() {
            return "tools.jar";
        }
    }

    );
}
