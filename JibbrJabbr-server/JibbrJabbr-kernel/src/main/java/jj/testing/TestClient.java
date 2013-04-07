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
package jj.testing;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jsoup.nodes.Document;

/**
 * @author jason
 *
 */
public interface TestClient {
	
	int status() throws Exception;
	int status(final long timeout, final TimeUnit unit) throws Exception;
	
	Throwable error() throws Exception;
	Throwable error(final long timeout, final TimeUnit unit) throws Exception;
	
	Map<String, String> headers() throws Exception;
	Map<String, String> headers(final long timeout, final TimeUnit unit) throws Exception;
	
	String contentsString() throws Exception;
	String contentsString(final long timeout, final TimeUnit unit) throws Exception;
	
	Document document() throws Exception;
	Document document(long timeout, TimeUnit unit) throws Exception;
	
	void dumpObjects();
}
