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
package jj.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import jj.resource.Resource;

/**
 * @author jason
 *
 */
public class MockHttpResponse implements HttpResponse {

	
	
	@Override
	public HttpResponseStatus status() {
		
		return null;
	}

	@Override
	public HttpResponse status(HttpResponseStatus status) {
		return this;
	}

	@Override
	public HttpResponse header(String name, String value) {
		return this;
	}

	@Override
	public HttpResponse headerIfNotSet(String name, String value) {
		return this;
	}

	@Override
	public HttpResponse headerIfNotSet(String name, long value) {
		return this;
	}

	@Override
	public boolean containsHeader(String name) {
		return false;
	}

	@Override
	public HttpResponse header(String name, Date date) {
		return this;
	}

	@Override
	public HttpResponse header(String name, long value) {
		return this;
	}

	@Override
	public List<Entry<String, String>> allHeaders() {
		return null;
	}

	@Override
	public HttpVersion version() {
		return HttpVersion.HTTP_1_1;
	}

	@Override
	public HttpResponse content(byte[] bytes) {
		return this;
	}

	@Override
	public HttpResponse content(ByteBuf buffer) {
		return this;
	}

	@Override
	public HttpResponse end() {
		return this;
	}

	@Override
	public void sendNotFound() {
	}

	@Override
	public void sendError(HttpResponseStatus status) {
	}

	@Override
	public HttpResponse sendNotModified(Resource resource) {
		return this;
	}

	@Override
	public HttpResponse sendNotModified(Resource resource, boolean cache) {
		return this;
	}

	@Override
	public HttpResponse sendTemporaryRedirect(Resource resource) {
		return this;
	}

	@Override
	public HttpResponse error(Throwable e) {
		return this;
	}

	@Override
	public Charset charset() {
		return StandardCharsets.UTF_8;
	}

	@Override
	public String header(String name) {
		return null;
	}

	@Override
	public String contentsString() {
		return null;
	}

	@Override
	public HttpResponse sendUncachedResource(Resource resource) throws IOException {
		return this;
	}

	@Override
	public HttpResponse sendCachedResource(Resource resource) throws IOException {
		return this;
	}

}
