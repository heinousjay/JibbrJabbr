/*
 *    Copyright 2012 Jason Miller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jj;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.grapher.GrapherModule;
import com.google.inject.grapher.InjectorGrapher;
import com.google.inject.grapher.graphviz.GraphvizModule;
import com.google.inject.grapher.graphviz.GraphvizRenderer;

public class Grapher {

	public static void main(String[] args) throws Exception {

		graphGood("main.dot", Guice.createInjector(new CoreModule(args, false)));
	}

	public final static void graphGood(String filename, Injector inj) {
		File file = new File(filename).getAbsoluteFile();
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintWriter out = new PrintWriter(baos);

			Injector injector =
				Guice.createInjector(new GrapherModule(), new GraphvizModule());
			GraphvizRenderer renderer =
				injector.getInstance(GraphvizRenderer.class);
			renderer.setOut(out).setRankdir("TB");

			injector.getInstance(InjectorGrapher.class).of(inj).graph();

			out = new PrintWriter(file, "UTF-8");
			String s = baos.toString("UTF-8");
			s = fixGrapherBug(s);
			s = hideClassPaths(s);
			s = s.replaceAll(" margin=(\\S+), ", " margin=\"$1\", ");
			out.write(s);
			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("wrote to " + file);
	}

	public static String hideClassPaths(String s) {
		s = s.replaceAll("\\w[a-z\\d_\\.]+\\.([A-Z][A-Za-z\\d_]*)", "");
		s = s.replaceAll("value=[\\w-]+", "random");
		return s;
	}

	public static String fixGrapherBug(String s) {
		s = s.replaceAll("style=invis", "style=solid");
		return s;
	}
}