/*******************************************************************************
 * (C) Copyright 2016 Jérôme Comte and Dorian Cransac
 *
 *  This file is part of djigger
 *
 *  djigger is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  djigger is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with djigger.  If not, see <http://www.gnu.org/licenses/>.
 *
 *******************************************************************************/
package io.djigger.ui.common;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

public class FileChooserHelper {

    private static String KEY_FILECHOOSER_LASTDIR = "filechooser.lastdir";

    private static class EnhancedJFileChooser extends JFileChooser {
        private enum SelectMode {
            LOAD,
            SAVE
        }

        private static File select(SelectMode mode, String title, FileFilter... preferredFilters) {
            // I'm not sure if this code really has the desired effect or why it's here in the first place,
            // but it seems to be a workaround for some display issues on Apple Macintoshs
            LookAndFeel lookAndFeelToRestore = null;
            if (System.getProperty("os.name").toLowerCase().trim().contains("mac")) {
                lookAndFeelToRestore = UIManager.getLookAndFeel();
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                    | UnsupportedLookAndFeelException e) {
                    e.printStackTrace();
                }
            }

            JFileChooser chooser = newChooser(title, preferredFilters);

            // restore L&F, see comment above
            if (lookAndFeelToRestore != null) {
                try {
                    UIManager.setLookAndFeel(lookAndFeelToRestore);
                } catch (UnsupportedLookAndFeelException e) {
                    e.printStackTrace();
                }
            }

            int dialogResult = mode == SelectMode.SAVE ? chooser.showSaveDialog(null) : chooser.showOpenDialog(null);

            if (dialogResult == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                setLastUsedDirectory(file);
                return file;
            }
            return null;
        }

        private EnhancedJFileChooser(File directory) {
            super(directory != null && directory.isDirectory() ? directory : null);
        }

        @Override
        public void approveSelection() {
            // Implementation note: result of getSelectedFile() may change throughout this method
            if (getDialogType() == SAVE_DIALOG) {
                if (getSelectedFile() != null) {
                    FileFilter filter = getFileFilter();
                    if (filter != null && !filter.accept(getSelectedFile())) {
                        // filter didn't like the file we chose
                        final File file = getSelectedFile();
                        if (filter instanceof ExtensionSuggestingFileNameFilter) {
                            ExtensionSuggestingFileNameFilter suggester = (ExtensionSuggestingFileNameFilter) filter;
                            File altFile = suggester.suggestAlternative(file);
                            if (!altFile.equals(file)) {
                                String title = "Save as " + altFile.getName()+" ?";
                                String message = "The file name you entered was: " +file.getName();
                                message += "\nBased on the current file filter, the suggested file name is: " +altFile.getName();
                                message += "\n\nPlease choose:";
                                message += "\n- YES to save as: " + altFile.getName();
                                message += "\n- NO to save as: " + file.getName();
                                message += "\n- CANCEL to return to the save dialog";
                                int selection = JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                                if (selection == JOptionPane.CANCEL_OPTION) {
                                    // do nothing, return to dialog
                                    return;
                                }
                                if (selection == JOptionPane.OK_OPTION) {
                                    setSelectedFile(altFile);
                                }
                            }
                        }
                    }
                    // user wants to write to given file, check if we need to warn because it exists
                    if (getSelectedFile().exists()) {
                        final File file = getSelectedFile();
                        if (file.isFile()) {
                            String title = "Overwrite existing file?";
                            String shortFilename = file.getName();
                            String message = "You are about to overwrite the file: " + shortFilename + "\nAre you sure?";
                            int selection = JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                            if (selection == JOptionPane.OK_OPTION) {
                                // approve: overwrite existing file
                                super.approveSelection();
                            }
                            // otherwise, just return to dialog
                        }
                    } else {
                        // approve: save to new (non-existing) file
                        super.approveSelection();
                    }
                } // otherwise: file was null, unexpected
            } else {
                // approve: load file
                super.approveSelection();
            }
        }
    }

    public static EnhancedJFileChooser newChooser(String dialogTitle, FileFilter... preferredFilters) {
        String lastDirName = Settings.getINSTANCE().getAsString(KEY_FILECHOOSER_LASTDIR);

        EnhancedJFileChooser chooser = new EnhancedJFileChooser(lastDirName != null ? new File(lastDirName) : null);

        if (dialogTitle != null) {
            chooser.setDialogTitle(dialogTitle);
        }

        // if supplied, use more specific file filters
        if (preferredFilters != null && preferredFilters.length > 0) {
            // add them to the list of choosable filters
            for (FileFilter filter : preferredFilters) {
                chooser.addChoosableFileFilter(filter);
            }
            // ... and use the first preferred filter as default
            chooser.setFileFilter(preferredFilters[0]);
        }

        return chooser;
    }

    /**
     * Sets the directory last used for filechooser operations. This is the directory which
     * will be set as the initial directory on subsequent file open/save operations.
     *
     * If the argument given is a directory, this is what will be saved.
     * If it is a file, the file's parent directory will be saved instead.
     *
     * @param file a file or directory from which to set the "last used" setting (see method javadoc)
     */
    public static void setLastUsedDirectory(File file) {
        if (file != null) {
            File directory = file;
            if (file.isFile()) {
                directory = file.getParentFile();
            }
            Settings.getINSTANCE().put(KEY_FILECHOOSER_LASTDIR, directory.getAbsolutePath());
        }
    }


    public static File loadFile(FileMetadata meta) {
        return EnhancedJFileChooser.select(EnhancedJFileChooser.SelectMode.LOAD, meta.loadDialogTitle, meta.preferredFilters);
    }

    public static File saveFile(FileMetadata meta) {
        return EnhancedJFileChooser.select(EnhancedJFileChooser.SelectMode.SAVE, meta.saveDialogTitle, meta.preferredFilters);
    }

}
