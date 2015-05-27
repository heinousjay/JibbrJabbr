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
package jj.system;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.HashSet;
import java.util.Set;

import jj.BootstrapClassPath;
import jj.system.Assets;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class AssetsTest {

	@Test
	public void test() {
		Set<String> paths = new HashSet<>();
		paths.add("/jj/assets");
		
		Assets assets = new Assets(new BootstrapClassPath(), paths);
		
		assertThat(assets.path("jj.js"), is(notNullValue()));
		
		// NEEDS A BETTER TEST
		assertThat(assets.path("jj1.js"), is(Assets.NOT_FOUND));
		
		
	}

}
