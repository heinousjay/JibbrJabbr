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
package jj.http.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jj.StringUtils;
import jj.resource.FileResource;
import io.netty.handler.codec.http.HttpHeaders;

/**
 * Helper to determine, from request headers and prepared response,  
 * what content to actually serve to the client
 * @author jason
 *
 */
class RangeHandler {
	
	/**
	 * 
	 */
	private static final String HEADER_PREFIX = "bytes=";
	private static final Pattern SPLITTER = Pattern.compile(",");
	private static final Pattern RANGE = Pattern.compile("(\\d*)-(\\d*)");
	
	static class Range implements Comparable<Range> {
		final long start;
		final long end;
		
		private Range(final long start, final long end) {
			this.start = start;
			this.end = end;
		}
		
		@Override
		public String toString() {
			return start + "-" + end;
		}

		@Override
		public int compareTo(Range o) {
			return (int)Math.signum(start - o.start);
		}
	}
	
	private final HttpHeaders requestHeaders;
	private final FileResource resource;
	private final List<Range> ranges = new ArrayList<>(2);
	private final List<Range> originalranges = new ArrayList<>(2);
	private final long overlapDistance;
	private final int maxRanges;
	private boolean badRequest = false;

	/**
	 * 
	 * @param requestHeaders The headers from the request
	 * @param resource The resource being served
	 * @param overlapDistance The amount of distance between consecutive ranges that should be coalesced
	 */
	RangeHandler(
		final HttpHeaders requestHeaders,
		final FileResource resource,
		final long overlapDistance
	) {
		this(requestHeaders, resource, overlapDistance, 5);
	}
	
	/**
	 * 
	 * @param requestHeaders The headers from the request
	 * @param resource The resource being served
	 * @param overlapDistance The amount of distance between consecutive ranges that should be coalesced
	 * @param maxRanges The maximum number of ranges that can be requested before bailing
	 */
	RangeHandler(
		final HttpHeaders requestHeaders,
		final FileResource resource,
		final long overlapDistance,
		final int maxRanges
	) {
		this.requestHeaders = requestHeaders;
		this.resource = resource;
		this.overlapDistance = overlapDistance;
		this.maxRanges = maxRanges;
		parseRanges();
	}
	
	private void parseRanges() {
		String value = requestHeaders.get(HttpHeaders.Names.RANGE);
		if (value == null) {
			ranges.add(new Range(0, resource.size() - 1));
		} else if (value.startsWith(HEADER_PREFIX)) {
			String[] candidates = SPLITTER.split(value.substring(HEADER_PREFIX.length()));
			
			if (candidates.length > maxRanges) {
				badRequest = true;
			} else {
				tryCandidates(candidates);
			}
		} else {
			badRequest = true;
		}
		
		if (!badRequest && !ranges.isEmpty()) {
			originalranges.addAll(ranges);
			coalesceRanges();
		} else {
			ranges.clear();
		}
	}

	private void tryCandidates(String[] candidates) {
		for (String candidate : candidates) {
			Matcher matcher = RANGE.matcher(candidate);
			if (matcher.matches()) {
				String group1 = matcher.group(1);
				String group2 = matcher.group(2);
				if (StringUtils.isEmpty(group1) && StringUtils.isEmpty(group2)) {
					badRequest = true;
					break;
				}
				Range range;
				if (StringUtils.isEmpty(group1)) {
					// it's a range from the end
					range = new Range(resource.size() - (Long.parseLong(group2) + 1), resource.size() - 1);
				} else if (StringUtils.isEmpty(group2)) {
					// it specifies a start to the end
					range = new Range(Long.parseLong(group1), resource.size() - 1);
				} else {
					// it's a normal range
					range = new Range(Long.parseLong(group1), Long.parseLong(group2));
				}
				if (range.start >= 0 && range.start <= range.end && range.start <= resource.size() - 1) {
					ranges.add(range.end > resource.size() - 1 ? new Range(range.start, resource.size() - 1) : range);
				}
				
			} else {
				badRequest = true;
				break;
			}
		}
	}

	/**
	 * 
	 */
	private void coalesceRanges() {
		Collections.sort(ranges);
		List<Range> adjustedRanges = makeAdjustedRanges();
		
		// now work through the adjusted ranges, removing anything that is redundant
		cleanOverlaps(adjustedRanges);
		
		ranges.clear();
		ranges.addAll(adjustedRanges);
	}

	private List<Range> makeAdjustedRanges() {
		List<Range> adjustedRanges = new ArrayList<>(ranges.size());
		for (Range current : ranges) {
			Range working = current;
			for (Range nextRange : ranges) {
				if (nextRange == working) continue;
				if (working.start < nextRange.start && working.end > nextRange.end) {
					continue;
				}
				if (
					working.start < nextRange.start &&
					working.end > (nextRange.start - overlapDistance)
				) {
					working = new Range(working.start, nextRange.end);
				}
			}
			adjustedRanges.add(working);
		}
		return adjustedRanges;
	}

	private void cleanOverlaps(List<Range> adjustedRanges) {
		long hwm = 0;
		for (Iterator<Range> i = adjustedRanges.iterator(); i.hasNext();) {
			Range range = i.next();
			if (range.end > hwm) {
				hwm = range.end;
			} else {
				i.remove();
			}
		}
	}
	
	public List<Range> ranges() {
		return Collections.unmodifiableList(ranges);
	}
	
	public boolean isBadRequest() {
		return badRequest;
	}
}
