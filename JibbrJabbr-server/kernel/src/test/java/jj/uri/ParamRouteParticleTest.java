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
package jj.uri;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class ParamRouteParticleTest {

	@Test
	public void test() {
		ParamRouteParticle particle = new ParamRouteParticle("{param}");
		
		assertThat(particle.matches("value"), is(true));
		
		HashMap<String, String> params = new HashMap<>();
		particle.populate("value", params);
		assertThat(params.get("param"), is("value"));
		
		
		List<String> contributions = new ArrayList<>();
		particle.contribute(contributions, params);
		assertThat(contributions.size(), is(1));
		assertThat(contributions.get(0), is("value"));
	}
}
