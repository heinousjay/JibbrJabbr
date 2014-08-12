var mockCollector = {
	addConfigurationMappedElement: function() {}
}

inject = function() {
	return mockCollector;
}

var valueOf = java.lang.String.valueOf;

describe('resource-properties.js', function() {
	
	describe('extensions', function() {
		
		it('requires the extension parameter to be a string', function() {
			expect(function() {
				module.exports.extension();
			}).toThrow(new Error("extension must be a string"));
			expect(function() {
				module.exports.extension(true);
			}).toThrow(new Error("extension must be a string"));
			expect(function() {
				module.exports.extension(1);
			}).toThrow(new Error("extension must be a string"));
		});
		
		it('does not allow . in the extension name', function() {
			expect(function() {
				module.exports.extension(".html");
			}).toThrow(new Error("extensions should only be letters, numbers, underscores, and dashes"));
		});
		
		it('only allows letters, numbers, underscores, and dashes', function() {
			expect(function() {
				module.exports.extension("()*");
			}).toThrow(new Error("extensions should only be letters, numbers, underscores, and dashes"));
		});
		
		it('requires an object argument to is', function() {
			expect(function() {
				module.exports.extension('ext').is();
			}).toThrow(new Error("is requires an object argument"));
			expect(function() {
				module.exports.extension('ext').is(1);
			}).toThrow(new Error("is requires an object argument"));
			expect(function() {
				module.exports.extension('ext').is("hi");
			}).toThrow(new Error("is requires an object argument"));
			expect(function() {
				module.exports.extension('ext').is(true);
			}).toThrow(new Error("is requires an object argument"));
		});
		
		it('requires mime type to be a present and a string', function() {
			expect(function() {
				module.exports.extension('ext').is({});
			}).toThrow(new Error("mimeType must be present and must be a string"));
			expect(function() {
				module.exports.extension('ext').is({mimeType: 1});
			}).toThrow(new Error("mimeType must be present and must be a string"));
			expect(function() {
				module.exports.extension('ext').is({mimeType: true});
			}).toThrow(new Error("mimeType must be present and must be a string"));
			expect(function() {
				module.exports.extension('ext').is({mimeType: []});
			}).toThrow(new Error("mimeType must be present and must be a string"));
			expect(function() {
				module.exports.extension('ext').is({mimeType: {}});
			}).toThrow(new Error("mimeType must be present and must be a string"));
		});
		
		it('requires a standards-compliant mime type', function() {
			expect(function() {
				module.exports.extension('ext').is({mimeType: 'not a mime type'});
			}).toThrow(new Error("mimeType must be a standard-compliant mime type"));
			expect(function() {
				module.exports.extension('ext').is({mimeType: 'invalid/type'});
			}).toThrow(new Error("invalid is not a valid media type"));
			
			// not sure what the subtype validation might be yet
		});
		
		it('requires charset to be a string, if present', function() {
			expect(function() {
				module.exports.extension('ext').is({mimeType: 'text/plain', charset: null});
			}).toThrow(new Error("charset must be a string if present"));
			expect(function() {
				module.exports.extension('ext').is({mimeType: 'text/plain', charset: 1});
			}).toThrow(new Error("charset must be a string if present"));
			expect(function() {
				module.exports.extension('ext').is({mimeType: 'text/plain', charset: true});
			}).toThrow(new Error("charset must be a string if present"));
			expect(function() {
				module.exports.extension('ext').is({mimeType: 'text/plain', charset: {}});
			}).toThrow(new Error("charset must be a string if present"));
		});
		
		it('rejects unrecognized charsets', function() {
			expect(function() {
				module.exports.extension('ext').is({mimeType: 'text/plain', charset: "whatever"});
			}).toThrow(new Error("whatever is not a recognized charset"));
		});
		
		it('correctly passes valid configurations to the collector', function() {
			spyOn(mockCollector, 'addConfigurationMappedElement');
			
			module.exports.extension('ext').is({mimeType:'text/plain', charset:'us-ascii', compressible: true});
			var call = mockCollector.addConfigurationMappedElement.calls.mostRecent();
			expect(call.args[0]).toBe('jj.resource.ResourceConfiguration.fileTypeSettings');
			expect(call.args[1]).toBe('ext');
			expect(call.args[2].mimeType()).toEqual(valueOf('text/plain'));
			expect(call.args[2].charset().name()).toEqual(valueOf('US-ASCII'));
			expect(call.args[2].compressible()).toBe(true);
			
			module.exports.extension('ext2').is({mimeType:'text/html', charset:'utf-8'});
			call = mockCollector.addConfigurationMappedElement.calls.mostRecent();
			expect(call.args[0]).toBe('jj.resource.ResourceConfiguration.fileTypeSettings');
			expect(call.args[1]).toBe('ext2');
			expect(call.args[2].mimeType()).toEqual(valueOf('text/html'));
			expect(call.args[2].charset().name()).toEqual(valueOf('UTF-8'));
			expect(call.args[2].compressible()).toBe(false);
		});
	});
});