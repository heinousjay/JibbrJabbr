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
package jj.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class UUIDUtilTest {

	@Test
	public void test() {
		UUID u1 = UUIDUtil.newRandomUUID();
		UUID u2 = UUIDUtil.newRandomUUID();
		UUID u3 = UUIDUtil.newRandomUUID();
		UUID u4 = UUIDUtil.newRandomUUID();
		UUID u5 = UUIDUtil.newRandomUUID();
		
		UUID src = UUID.randomUUID();
		
		assertThat(u1.variant(), is(src.variant()));
		assertThat(u2.variant(), is(src.variant()));
		assertThat(u3.variant(), is(src.variant()));
		assertThat(u4.variant(), is(src.variant()));
		assertThat(u5.variant(), is(src.variant()));
		
		assertThat(u1.version(), is(src.version()));
		assertThat(u2.version(), is(src.version()));
		assertThat(u3.version(), is(src.version()));
		assertThat(u4.version(), is(src.version()));
		assertThat(u5.version(), is(src.version()));
		
		assertThat(u1, is(not(u2)));
		assertThat(u1, is(not(u3)));
		assertThat(u1, is(not(u4)));
		assertThat(u1, is(not(u5)));
		
		assertThat(u2, is(not(u3)));
		assertThat(u2, is(not(u4)));
		assertThat(u2, is(not(u5)));
		
		assertThat(u3, is(not(u4)));
		assertThat(u3, is(not(u5)));

		assertThat(u4, is(not(u5)));
		
		// eh, don't know what else to check. even this might be overkill lol
	}

}
