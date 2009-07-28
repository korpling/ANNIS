/*
 * Copyright 2009 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import annis.frontend.servlets.visualizers.Visualizer;

public class PartiturTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String fileNameInput = "/Users/black/Desktop/inline_example.xml";
			String fileNameOutput = "/Users/black/Desktop/mmax.html";
			String visualizerClassName = "annis.frontend.servlets.visualizers.MmaxVisualizer";
			String namespace = "mmax";
			
			File output = new File(fileNameOutput);
			StringBuffer paula = new StringBuffer();
			FileReader fReader = new FileReader(new File(fileNameInput));
			BufferedReader bReader = new BufferedReader(fReader);
			char[] cbuf = new char[256];
			int i = 0;
			while((i= bReader.read(cbuf)) > 0) {
				for(i=i;i<cbuf.length;i++)
					cbuf[i] = 0x00;
				paula.append(cbuf);
			}
			
			FileWriter fw = new FileWriter(output);
						
			ClassLoader classLoader = Visualizer.class.getClassLoader();
			Map<String, String> markableMap = new HashMap<String, String>();
			markableMap.put("id_20", "red");
			markableMap.put("257307", "red");
		
			Visualizer visualizer = (Visualizer) classLoader.loadClass(visualizerClassName).newInstance();
			visualizer.setNamespace(namespace);
			visualizer.setMarkableMap(markableMap);
			//response.setCharacterEncoding(visualizer.getCharacterEncoding());
			//response.setContentType(visualizer.getContentType()); 
			
			visualizer.setPaula(paula.toString().trim());
			visualizer.writeOutput(fw);
			fw.close();
			} catch (InstantiationException e1) {
				e1.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}
