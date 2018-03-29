package io.djigger.ui.common;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public class ExtensionSuggestingFileNameFilter extends FileFilter {
    // unfortunately, FileNameExtensionFilter is final, so we can't simply extend, but have to delegate
    private final FileNameExtensionFilter delegate;
    private final String extension;

    public ExtensionSuggestingFileNameFilter(String description, String extension) {
        this.extension = extension;
        this.delegate = new FileNameExtensionFilter(description, extension);
    }

    @Override
    public boolean accept(File f) {
        return delegate.accept(f);
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    public File suggestAlternative(File file) {
        File parent = file.getParentFile();
        String fileName = file.getName();
        String altFileName = fileName + "." + extension;

        return new File(parent, altFileName);
    }
}
