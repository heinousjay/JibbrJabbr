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

import static jj.repl.telnet.TelnetProtocol.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * encapsulates the current status of a telnet connection
 * 
 * @author jason
 *
 */
class TelnetSessionStatus {
	
	TelnetSessionStatus() {}
	
	
	
	TelnetSessionStatus parseResponse(final ByteBuf byteBuf) {
		int index = Unpooled.wrappedBuffer(byteBuf).forEachByte(this::nextToken);
		System.out.println(index);
		return this;
	}
	
	private enum State {
		AwaitingIAC,       // obv.
		AwaitingMode,      // WILL, WONT, DO, DONT, SE, SB, IAC
		AwaitingCommand,   // just so
		AwaitingSubcommand,
		AwaitingSubcommandEnd
	}
	
	private State state = State.AwaitingIAC;
	private int mode = 0;
	
	private boolean nextToken(byte value) {
		switch (state) {
		case AwaitingIAC:
			if (value == (byte)IAC) {
				System.out.println("IAC");
				state = State.AwaitingMode;
			} else {
				// pass the byte through
				System.out.println("got " + value );
			}
			break;
			
		case AwaitingMode:
			switch (value) {
			case (byte)WILL:
				System.out.println("WILL");
				mode = WILL;
				state = State.AwaitingCommand;
				break;
				
			case (byte)WONT:
				System.out.println("WONT");
				mode = WONT;
				state = State.AwaitingCommand;
				break;
				
			case (byte)DO:
				System.out.println("DO");
				mode = DO;
				state = State.AwaitingCommand;
				break;
				
			case (byte)DONT:
				System.out.println("DONT");
				mode = DONT;
				state = State.AwaitingCommand;
				break;
				
			case (byte)SE:
			case (byte)SB:
				
				break;
			case (byte)IAC:
				state = State.AwaitingIAC;
				// pass the 255 through
				break;
			default:
				throw new AssertionError("what");
			}
			
			break;
			
		case AwaitingCommand:
			Command command = Command.commandFor(value);
			System.out.println(command);
			switch (mode) {
			case WILL:
				
				break;
				
			case WONT:
				
				break;
				
			case DO:
				
				break;
				
			case DONT:
				
				break;
			}
			state = State.AwaitingIAC;
			break;
			
		default:
			throw new AssertionError("fell through!");
		}
		return true;
	}
	
//	IAC WILL AUTHENTICATION      -- ff fb 25
//	IAC DO SUPPRESS-GO-AHEAD     -- ff fd 3
//	IAC WILL TERMINAL-TYPE       -- ff fb 18
//	IAC WILL NAWS                -- ff fb 1f
//	IAC WILL TERMINAL-SPEED      -- ff fb 20
//	IAC WILL TOGGLE-FLOW-CONTROL -- ff fb 21
//	IAC WILL LINEMODE            -- ff fb 22
//	IAC WILL NEW-ENVIRON         -- ff fb 27
//	IAC DO STATUS                -- ff fd 5
//	IAC WILL X-DISPLAY-LOCATION  -- ff fb 23

	private boolean linemode = true;
	
	boolean linemode() {
		return linemode;
	}
	
	
	// SLC_SYNCH 3 0
	
	
	// SLC_IP 62 3
	// SLC_AO 2 f
	// SLC_AYT 2 14
	// SLC_ABORT 62 1c
	// SLC_EOF 2 4
	// SLC_SUSP 42 1a
	// SLC_EC 2 7f
	// SLC_EL 2 15
	// SLC_EW 2 17
	// SLC_RP 2 12
	// SLC_LNEXT 2 16
	// SLC_XON 2 11
	// SLC_XOFF 2 13
	// SLC_FORW1 0 FF
	// SLC_FORW2 0 FF
}
