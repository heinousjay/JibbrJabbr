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

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

/**
 * Keys for logging for the Kernel subsystems
 * 
 * @author Jason Miller
 *
 */
@BaseName("jj.kernel-messages")
@LocaleData(
	defaultCharset="UTF8",
	value = {
		@Locale("en")
	}
)
public enum KernelMessages {
	// general messages
	ObjectInstantiating,
	ObjectInstantiated,
	ObjectDisposing,
	
	KernelInitialized,
	UsingUnknownLogger,
	ReturningLogger,
	
	LoopThreadName,
	
	// HttpServer messages
	BindingPort,
	InterfaceBound,
	ReachedStartSyncPoint,
	ConnectionsRemainPastTimeout,
	HttpServerResourcesReleasing,
	HttpServerResourcesReleased,
	
	// NettyRequestBridge messages
	ServerErrorFallbackResponse,
	
	// JJThreadPoolExecutor messages
	JJTaskStarting,
	JJThreadInitializing,
	JJThreadStarting,
	JJThreadExiting,
	JJTaskEnded,
	JJTaskEndedWithException,
	
	// SynchronousThreadPoolExecutor messages
	SynchronousTaskRejected,
	SynchronousTaskDone,
	SynchronousThreadName,
	
	// AsynchronousThreadPoolExecutor messages
	AsynchronousTaskRejected,
	AsynchronousTaskDone,
	AsynchronousThreadName
}
