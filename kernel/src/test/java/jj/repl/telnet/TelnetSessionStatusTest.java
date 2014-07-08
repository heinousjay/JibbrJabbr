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
package jj.repl.telnet;

import jj.repl.telnet.TelnetSessionStatus;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.junit.Test;

/**
 * @author jason
 *
 */
public class TelnetSessionStatusTest {
	
	String commands1 = "ff fb 25 ff fd 3 ff fb 18 ff fb 1f ff fb 20 ff fb 21 ff fb 22 ff fb 27 ff fd 5 ff fb 23";

	private ByteBuf toByteBuf(String commands) {
		String[] bytes = commands.split("\\s+");
		
		ByteBuf result = Unpooled.buffer(bytes.length, bytes.length);
		for (String b : bytes) {
			result.writeByte(Integer.parseInt(b, 16));
		}
		return result;
	}
	
	@Test
	public void test() {
		new TelnetSessionStatus().parseResponse(toByteBuf(commands1));
	}

}
