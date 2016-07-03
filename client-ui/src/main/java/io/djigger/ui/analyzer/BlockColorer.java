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
package io.djigger.ui.analyzer;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class BlockColorer implements Serializable {
	
	private static final Logger logger = LoggerFactory.getLogger(BlockColorer.class);
	
	public final List<Framework> frameworks;

	public BlockColorer() {
		frameworks = new ArrayList<Framework>();
		frameworks.add(new Framework("Logging",Color.RED, "\\.log"));
		frameworks.add(new Framework("Hibernate",Color.MAGENTA, "hibernate"));
		frameworks.add(new Framework("JDBC",Color.GREEN, "oracle\\.jdbc\\.driver"));
		frameworks.add(new Framework("JPA",Color.MAGENTA, "openjpa"));
	}

	public BlockColorer(File file) {
		super();
		frameworks = new ArrayList<Framework>();
		loadFrameworks(file);
	}

	public Framework match(String methodname) {
		for(Framework framework:frameworks) {
			Matcher matcher = framework.getMatchingPattern().matcher(methodname);
			if(matcher.find()) {
				return framework;
			}
		}
		return null;
	}

	public Color matchAndGetColor(String methodname) {
		Framework framework = match(methodname);
		return framework!=null?framework.getColor():Color.LIGHT_GRAY;
	}

	public class Framework implements Serializable {
		private String name;

		private Color color;

		private Pattern matchingPattern;

		public Framework(String name, Color color, String matchingPatternStr) {
			super();
			this.name = name;
			this.color = color;
			this.matchingPattern = Pattern.compile(matchingPatternStr);
		}

		public String getName() {
			return name;
		}

		public Color getColor() {
			return color;
		}

		public Pattern getMatchingPattern() {
			return matchingPattern;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setMatchingPattern(String matchingPattern) {
			this.matchingPattern = Pattern.compile(matchingPattern);
		}

		public void setColor(Color color) {
			this.color = color;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Framework other = (Framework) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		private BlockColorer getOuterType() {
			return BlockColorer.this;
		}

		private String exportFormat() {
			return name + ";" + matchingPattern.pattern() + ";" + color.getRed() + "," + color.getGreen() + "," + color.getBlue();
		}
	}

	public List<Framework> getFrameworks() {
		return frameworks;
	}

	public static BlockColorer load(File file) {
		ObjectInputStream stream = null;
		try {
			stream = new ObjectInputStream(new FileInputStream(file));
			Object o = stream.readObject();
			if(o instanceof BlockColorer) {
				return (BlockColorer)o;
			} else {
				return null;
			}
		} catch (IOException e) {
			logger.error("Unable load the bloc colorer from file " + file, e);
			return null;
		} catch (ClassNotFoundException e) {
			logger.error("Unable load the bloc colorer from file " + file, e);
			return null;
		} catch (Exception e) {
			return null;
		} finally {
			if(stream != null) {
				try {
					stream.close();
				} catch (IOException e) {}
			}
		}
	}

	public void loadFrameworks(File file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while((line=reader.readLine())!=null) {
				addFramework(line);
			}
		} catch (FileNotFoundException e) {
			logger.error("Unable load framweork definition from file " + file, e);
		} catch (IOException e) {
			logger.error("Unable load framweork definition from file " + file, e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {}
		}
	}
	
	public void clearFrameworks() {
		frameworks.clear();
	}

	public void addFramework(String line) {
		String[] split = line.split(";");
		if(split.length==3) {
				String[] cStr = split[2].split(",");
				if(cStr.length==3) {
					Color color = new Color(Integer.decode(cStr[0]),
								Integer.decode(cStr[1]),Integer.decode(cStr[2]));
					frameworks.add(new Framework(split[0], color, split[1]));
				} else {
					throw new NumberFormatException();
				}
		} else {
			throw new NumberFormatException();
		}
	}

	public void export(File file) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(file));
			for(Framework f:frameworks) {
				writer.println(f.exportFormat());
			}

		} catch (IOException e) {
			logger.error("Unable to export framework definition to file " + file, e);
		} finally {
			writer.close();
		}
	}
}
