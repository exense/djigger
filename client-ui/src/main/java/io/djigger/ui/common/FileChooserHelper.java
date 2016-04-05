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

import java.io.File;

import javax.swing.JFileChooser;

public class FileChooserHelper {

	public static File selectFile(String dialogTitle, String buttonText) {
		String lastDir = Settings.getINSTANCE().getAsString("filechooser.lastdir");
		JFileChooser chooser;
		if(lastDir!=null) {
			chooser = new JFileChooser(new File(lastDir));
		} else {
			chooser = new JFileChooser();
		}
		
		chooser.setDialogTitle(dialogTitle);
		chooser.setApproveButtonText(buttonText);
		
		int result = chooser.showOpenDialog(null);
		if(result == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			File dir = file.getParentFile();
			Settings.getINSTANCE().put("filechooser.lastdir", dir.getAbsolutePath());
			
			return file;
		} else {
			return null;
		}
	}
	
	
}
