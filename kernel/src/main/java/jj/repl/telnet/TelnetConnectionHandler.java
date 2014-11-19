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
import static io.netty.buffer.Unpooled.*;

import javax.inject.Inject;

import jj.execution.ServerTask;
import jj.execution.TaskRunner;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * accepts a connection to the repl server, assumes it's telnet and tries to
 * negotiate a session using UTF-8 with no client echo in character mode.
 * 
 * depending on what it negotiates here, it further configures the pipeline
 * 
 * assumption: once negotiation is complete, no more control messages will
 * happen! so any IACs will simply be read as unknown characters
 * 
 * @author jason
 *
 */
class TelnetConnectionHandler extends SimpleChannelInboundHandler<ByteBuf> {
	
	static final ByteBuf START_CHARSET_NEGOTIATION = makeMessage(IAC, WILL, CHARSET);
	
	static final ByteBuf CHARSET_NEGOTIATION_ACCEPTED = makeMessage(IAC, DO, CHARSET);
	
	static final ByteBuf CHARSET_NEGOTIATION_REJECTED = makeMessage(IAC, DONT, CHARSET);
	
	static final ByteBuf TURN_ECHO_OFF = makeMessage(IAC, WILL, ECHO);
	
	static final ByteBuf ECHO_OFF_ACCEPTED = makeMessage(IAC, DO, ECHO);
	
	static final ByteBuf ECHO_OFF_REJECTED = makeMessage(IAC, DONT, ECHO);
	
	static final ByteBuf START_LINEMODE_NEGOTIATION = makeMessage(IAC, WILL, LINEMODE);
	
	static final ByteBuf LINEMODE_NEGOTIATION_ACCEPTED = makeMessage(IAC, DO, LINEMODE);
	
	static final ByteBuf LINEMODE_NEGOTIATION_REJECTED = makeMessage(IAC, DONT, LINEMODE);
	
	private static ByteBuf makeMessage(int...bytes) {
		ByteBuf result = buffer(bytes.length, bytes.length);
		for (int b : bytes) {
			result.writeByte(b);
		}
		return result;
	}
	
	private enum State {
		AwaitingClientNegotiation,
		AwaitingWillTransmitBinary,
		AwaitingWillNegotiateCharset,
		Done; // technically not a state
	}
	
	private final TaskRunner taskRunner;
	private State state;
	
	@Inject
	TelnetConnectionHandler(final TaskRunner taskRunner) {
		this.taskRunner = taskRunner;
	}
	
	/**
	 * this is a timer that runs in 1/4 second after connection.  if we didn't receive an opening salvo
	 * of telnet negotiation commands, it's a dumb client and we just move on with line-mode REPL
	 * 
	 * otherwise, try to negotiate the terminal we want for niceness
	 */
	private final ServerTask awaitingInitialNegotiation = new ServerTask("awaiting opening telnet negotiation") {

		@Override
		protected void run() throws Exception {
			System.out.println("negotiation not received. assuming dumb client");
			state = State.Done;
		}
		
		@Override
		protected long delay() {
			return 250;
		}
	};
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		// sanity check
		assert state == null : "this handler cannot be reused";
		
		// start a timer, if we receive nothing in x millis, try to go into binary mode
		// otherwise, we may get an opening negotiation
		state = State.AwaitingClientNegotiation;
		taskRunner.execute(awaitingInitialNegotiation);
		
	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
		dumpBuffer("received", msg);
		// sanity check
		assert state != null : "got into read without expecting anything next";
		switch (state) {
		
		case AwaitingClientNegotiation:
			// cancel the awaiting timer
			awaitingInitialNegotiation.cancelKey().cancel();
			// try to read it
			ctx.writeAndFlush(makeMessage(IAC, DO, TRANSMIT_BINARY));
			//ctx.writeAndFlush(makeMessage(IAC, SB, TERMINAL_TYPE, TerminalType.SEND, IAC, SE));
			state = State.AwaitingWillTransmitBinary;
			break;
			
		case AwaitingWillTransmitBinary:
			if (msg.equals(makeMessage(IAC, WILL, TRANSMIT_BINARY))) {
				ctx.writeAndFlush(makeMessage(IAC, WILL, CHARSET));
				state = State.AwaitingWillNegotiateCharset;
			} else {
				state = State.Done;
			}
			break;
			
		case AwaitingWillNegotiateCharset:
			
			dumpBuffer("charset!", msg);
			state = State.Done;
			break;
			
		default:
			ctx.fireChannelRead(msg);
			
		}
	}
	
	
	
	private void dumpBuffer(String message, ByteBuf buffer) {
		System.out.println(message);
		buffer.forEachByte(new ByteBufProcessor() {
			
			@Override
			public boolean process(byte value) throws Exception {
				// TODO Auto-generated method stub
				System.out.print(Integer.toHexString(((int)value) & 255));
				System.out.print(" ");
				return true;
			}
		});
		System.out.println(); // and linebreak
	}

}
