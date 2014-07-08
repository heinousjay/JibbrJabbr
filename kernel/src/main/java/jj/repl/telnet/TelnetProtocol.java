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

/**
 * OLD SCHOOL CONSTANTS BB YEAH
 * 
 * i want enums.  maybe, maybe
 * 
 * @author jason
 *
 */
enum TelnetProtocol {
	;
	
	enum Command {
		TRANSMIT_BINARY(0x00),
		SUPPRESS_GO_AHEAD(0x03),
		STATUS(0x05),
		TERMINAL_TYPE(0x18),
		NAWS(0x1F),
		TERMINAL_SPEED(0x20),
		TOGGLE_FLOW_CONTROL(0x21),
		LINEMODE(0x22),
		X_DISPLAY_LOCATION(0x23),
		AUTHENTICATION(0x25),
		NEW_ENVIRON(0x27)
		;
		
		final int value;
		private Command(int value) {
			this.value = value;
		}
		
		int value() {
			return value;
		}
		
		static Command commandFor(byte b) {
			for (Command c : values()) {
				if (b == (byte)c.value) {
					return c;
				}
			}
			return null;
			
		}
	}
	
	// decoded from the standard telnet client on Mac OS X 10.8.5 when started with
	// telnet localhost -9955
	// the dash tells it to behave like normal telnet over a non-standard port
	// this is sent immediately upon connection, which totally makes sense
//		IAC WILL AUTHENTICATION      -- ff fb 25
//		IAC DO SUPPRESS-GO-AHEAD     -- ff fd 3
//		IAC WILL TERMINAL-TYPE       -- ff fb 18
//		IAC WILL NAWS                -- ff fb 1f
//		IAC WILL TERMINAL-SPEED      -- ff fb 20
//		IAC WILL TOGGLE-FLOW-CONTROL -- ff fb 21
//		IAC WILL LINEMODE            -- ff fb 22
//		IAC WILL NEW-ENVIRON         -- ff fb 27
//		IAC DO STATUS                -- ff fd 5
//		IAC WILL X-DISPLAY-LOCATION  -- ff fb 23

	static final int IAC  = 0xFF;
	static final int DONT = 0xFE;
	static final int DO   = 0xFD;
	static final int WONT = 0xFC;
	static final int WILL = 0xFB;
	
	static final int SE = 0xF0; // F0 - subprotocol negotiation end
	static final int SB = 0xFA; // FA - subprotocol negotiation start
	
	static final int TRANSMIT_BINARY = 0x00;
	
	// we handle echo if we can get in to character mode
	static final int ECHO = 0x01;
	
	// suppressing go-ahead is vital to getting into character mode
	static final int SUPPRESS_GO_AHEAD = 0x03; 
	
	
	static final int TERMINAL_TYPE = 0x18;
	
	static interface TerminalType {
		static final int IS   = 0x00;
		static final int SEND = 0x01;
	}
	
	static final int LINEMODE = 0x22;
	
	static interface Linemode {
		static final int MODE         = 0x01;
		//
		static final int FORWARD_MASK = 0x02;
		
		/** Setting of Local Characters */
		static final int SLC          = 0x03;
		
		static interface SlcFunction {
			static final int SLC_SYNCH = 0x01;
			static final int SLC_BRK   = 0x02;
			static final int SLC_IP    = 0x03;
			static final int SLC_AO    = 0x04;
			static final int SLC_AYT   = 0x05;
			static final int SLC_EOR   = 0x06;
			static final int SLC_ABORT = 0x07;
			static final int SLC_EOF   = 0x08;
			static final int SLC_SUSP  = 0x09;
			static final int SLC_EC    = 0x0A;
			static final int SLC_EL    = 0x0B;
			static final int SLC_EW    = 0x0C;
			static final int SLC_RP    = 0x0D;
			static final int SLC_LNEXT = 0x0E;
			static final int SLC_XON   = 0x0F;
			static final int SLC_XOFF  = 0x10;
			static final int SLC_FORW1 = 0x11;
			static final int SLC_FORW2 = 0x12;
			static final int SLC_MCL   = 0x13;
			static final int SLC_MCR   = 0x14;
			static final int SLC_MCWL  = 0x15;
			static final int SLC_MCWR  = 0x16;
			static final int SLC_MCBOL = 0x17;
			static final int SLC_MCEOL = 0x18;
			static final int SLC_INSRT = 0x19;
			static final int SLC_OVER  = 0x1A;
			static final int SLC_ECR   = 0x1B;
			static final int SLC_EWR   = 0x1C;
			static final int SLC_EBOL  = 0x1D;
			static final int SLC_EEOL  = 0x1E;
		}
		
		static interface SlcLevelBits {
			static final int SLC_NOSUPPORT  = 0x00;
			static final int SLC_CANTCHANGE = 0x01;
			static final int SLC_VALUE      = 0x02;
			static final int SLC_DEFAULT    = 0x03;
			
			static final int MASK = 0x03; // and check for equality with above
		}
		
		static interface SlcSomething {
			static final int SLC_ACK      = 0b10000000;
			static final int SLC_FLUSHIN  = 0b01000000;
			static final int SLC_FLUSHOUT = 0b00100000;
			
			static final int sixtytwo = 0b00111110;
			static final int fortytwo = 0b00101010;
		}
	}
	
	// the following was sent by the standard telnet in Mac OS X 10.8.5 in response
	// to IAC DO LINEMODE
	
	// IAC SB LINEMODE SLC - negotiate linemode slc, followed by config triplets
	// SLC_SYNCH DEFAULT         00 - 
	// SLC_IP    FLUSHOUT+VALUE  03 - (was 62?  no clue what the middle 3 bits mean)
	// SLC_AO    VALUE           0f
	// SLC_AYT   VALUE           14
	// SLC_ABORT FLUSHOUT+VALUE  1c - (was 62?  no clue what the middle 3 bits mean)
	// SLC_EOF   VALUE           04
	// SLC_SUSP  FLUSHOUT+VALUE  1a - (was 42?  no clue what the middle bit means)
	// SLC_EC    VALUE           7f
	// SLC_EL    VALUE           15
	// SLC_EW    VALUE           17
	// SLC_RP    VALUE           12
	// SLC_LNEXT VALUE           16
	// SLC_XON   VALUE           11
	// SLC_XOFF  VALUE           13
	// SLC_FORW1 NOSUPPORT       FF - seems the value here is just 255 because why not.
	// SLC_FORW2 NOSUPPORT       FF - can't find a spec.  maybe it's to test the receiving end for compliance
	// IAC SE - negotiation over
	
	// really wants me to suppress go ahead haha
	// IAC DO SUPPRESS_GO_AHEAD
	
	static final int AUTHENTICATION = 0x25;
	// currently do not support this, so only WONT response for now
	
	static final int CHARSET                 = 0x2A;
	
	static interface Charset {
		
		static final int REQUEST         = 0x01;
		static final int ACCEPTED        = 0x02;
		static final int REJECTED        = 0x03;
		static final int TTABLE_IS       = 0x04;
		static final int TTABLE_REJECTED = 0x05;
		static final int TTABLE_ACK      = 0x06;
		static final int TTABLE_NAK      = 0x07;
	}

//	00: TRANSMIT-BINARY
//	01: ECHO
//	03: SUPPRESS-GO-AHEAD
//	05: STATUS
//	06: TIMING-MARK
//	0A: NAOCRD
//	0B: NAOHTS
//	0C: NAOHTD
//	0D: NAOFFD
//	0E: NAOVTS
//	0F: NAOVTD
//	10: NAOLFD
//	11: EXTEND-ASCII
//	12: LOGOUT
//	13: BM
//	14: DET	
//	17: SEND-LOCATION
//	18: TERMINAL-TYPE
//	19: END-OF-RECORD
//	1A: TUID
//	1B: OUTMRK
//	1C: TTYLOC
//	1D: 3270-REGIME
//	1E: X.3-PAD
//	1F: NAWS
//	20: TERMINAL-SPEED
//	21: TOGGLE-FLOW-CONTROL
//	22: LINEMODE
//	23: X-DISPLAY-LOCATION
//	24: ENVIRON
//	25: AUTHENTICATION
//	26: ENCRYPT
//	27: NEW-ENVIRON
//	28: TN3270E
//	2A: CHARSET
//	2C: COM-PORT-OPTION
//	2F: KERMIT
}
