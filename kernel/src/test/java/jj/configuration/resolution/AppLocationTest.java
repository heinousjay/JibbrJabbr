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
package jj.configuration.resolution;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.*;
import static jj.configuration.resolution.AppLocation.*;

import java.util.List;

import jj.resource.Location;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class AppLocationTest {

	@Test
	public void test() {
		List<Location> locations = Base.and(Private).and(Public).locations();
		
		assertThat(locations, contains((Location)Base, Private, Public));
	}

}
