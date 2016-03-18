/*******************************************************************************
 * (C) Copyright  2016 Jérôme Comte and others.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Contributors:
 *    - Jérôme Comte
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
