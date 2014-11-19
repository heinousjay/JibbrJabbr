
var support = {
	addToMap: function() {},
	accumulateError: function() {}
}

require = function() {
	return function(b) {
		return support;
	}
}

var valueOf = java.lang.String.valueOf;

describe('resource-properties.js', function() {
	
	var name = 'fileTypeSettings';
	
	beforeEach(function() {
		spyOn(support, 'addToMap');
		spyOn(support, 'accumulateError').and.returnValue(true);
	});
	
	describe('extensions', function() {
		
		describe('errors', function() {
			
			afterEach(function() {
				expect(support.addToMap).not.toHaveBeenCalled();
			});
			
			it('requires the extension parameter to be a string', function() {
				
				expect(module.exports.extension().is()).toBeDefined();
				expect(support.accumulateError).toHaveBeenCalledWith(name, "extension must be a string");
				support.accumulateError.calls.reset();
				
				expect(module.exports.extension(true).is()).toBeDefined();
				expect(support.accumulateError).toHaveBeenCalledWith(name, "extension must be a string");
				support.accumulateError.calls.reset();
				
				expect(module.exports.extension(1).is()).toBeDefined();
				expect(support.accumulateError).toHaveBeenCalledWith(name, "extension must be a string");
			});
			
			it('does not allow . in the extension name', function() {
				module.exports.extension(".html");
				expect(support.accumulateError).toHaveBeenCalledWith(name, "extensions should only be letters, numbers, underscores, and dashes");
			});
			
			it('only allows letters, numbers, underscores, and dashes', function() {
				module.exports.extension("()*");
				expect(support.accumulateError).toHaveBeenCalledWith(name, "extensions should only be letters, numbers, underscores, and dashes");
			});
			
			it('requires an object argument to is', function() {
				module.exports.extension('ext').is();
				expect(support.accumulateError).toHaveBeenCalledWith(name, "is requires an object argument");
				support.accumulateError.calls.reset();
			
				module.exports.extension('ext').is(1);
				expect(support.accumulateError).toHaveBeenCalledWith(name, "is requires an object argument");
				support.accumulateError.calls.reset();
				
	
				module.exports.extension('ext').is("hi");
				expect(support.accumulateError).toHaveBeenCalledWith(name, "is requires an object argument");
				support.accumulateError.calls.reset();
				
	
				module.exports.extension('ext').is(true);
				expect(support.accumulateError).toHaveBeenCalledWith(name, "is requires an object argument");
			});
			
			it('requires mime type to be a present and a string', function() {
				module.exports.extension('ext').is({});
				expect(support.accumulateError).toHaveBeenCalledWith(name, "mimeType must be present and must be a string");
				support.accumulateError.calls.reset();
				
				module.exports.extension('ext').is({mimeType: 1});
				expect(support.accumulateError).toHaveBeenCalledWith(name, "mimeType must be present and must be a string");
				support.accumulateError.calls.reset();
				
				module.exports.extension('ext').is({mimeType: true});
				expect(support.accumulateError).toHaveBeenCalledWith(name, "mimeType must be present and must be a string");
				support.accumulateError.calls.reset();
				
				module.exports.extension('ext').is({mimeType: []});
				expect(support.accumulateError).toHaveBeenCalledWith(name, "mimeType must be present and must be a string");
				support.accumulateError.calls.reset();
				
				module.exports.extension('ext').is({mimeType: {}});
				expect(support.accumulateError).toHaveBeenCalledWith(name, "mimeType must be present and must be a string");
			});
			
			it('requires a standards-compliant mime type', function() {
				module.exports.extension('ext').is({mimeType: 'not a mime type'});
				expect(support.accumulateError).toHaveBeenCalledWith(name, "mimeType must be a standard-compliant mime type");
				support.accumulateError.calls.reset();
				
				module.exports.extension('ext').is({mimeType: 'invalid/type'});
				expect(support.accumulateError).toHaveBeenCalledWith(name, "invalid is not a valid media type");
				
				// not sure what the subtype validation might be yet
			});
			
			it('requires charset to be a string, if present', function() {
				
				module.exports.extension('ext').is({mimeType: 'text/plain', charset: null});
				expect(support.accumulateError).toHaveBeenCalledWith(name, "charset must be a string if present");
				support.accumulateError.calls.reset();
				
				module.exports.extension('ext').is({mimeType: 'text/plain', charset: 1});
				expect(support.accumulateError).toHaveBeenCalledWith(name, "charset must be a string if present");
				support.accumulateError.calls.reset();
				
				module.exports.extension('ext').is({mimeType: 'text/plain', charset: true});
				expect(support.accumulateError).toHaveBeenCalledWith(name, "charset must be a string if present");
				support.accumulateError.calls.reset();
				
				module.exports.extension('ext').is({mimeType: 'text/plain', charset: {}});
				expect(support.accumulateError).toHaveBeenCalledWith(name, "charset must be a string if present");
			});
			
			it('requires compressible to be a boolean, if present', function() {
				
				module.exports.extension('ext').is({mimeType: 'text/plain', compressible: null});
				expect(support.accumulateError).toHaveBeenCalledWith(name, "compressible must be a boolean if present");
				support.accumulateError.calls.reset();
	
				module.exports.extension('ext').is({mimeType: 'text/plain', compressible: 1});
				expect(support.accumulateError).toHaveBeenCalledWith(name, "compressible must be a boolean if present");
				support.accumulateError.calls.reset();
	
				module.exports.extension('ext').is({mimeType: 'text/plain', compressible: {}});
				expect(support.accumulateError).toHaveBeenCalledWith(name, "compressible must be a boolean if present");
				support.accumulateError.calls.reset();
	
				module.exports.extension('ext').is({mimeType: 'text/plain', compressible: "true"});
				expect(support.accumulateError).toHaveBeenCalledWith(name, "compressible must be a boolean if present");
			});
			
			it('rejects unrecognized charsets', function() {
				module.exports.extension('ext').is({mimeType: 'text/plain', charset: "whatever"});
				expect(support.accumulateError).toHaveBeenCalledWith(name, "whatever is not a recognized charset");
			});
			
		});
		
		it('correctly passes valid configurations to the collector', function() {
			
			module.exports.extension('ext').is({mimeType:'text/plain', charset:'us-ascii', compressible: true});
			var call = support.addToMap.calls.mostRecent();
			expect(call.args[0]).toBe(name);
			expect(call.args[1]).toBe('ext');
			expect(call.args[2].mimeType()).toEqual(valueOf('text/plain'));
			expect(call.args[2].charset().name()).toEqual(valueOf('US-ASCII'));
			expect(call.args[2].compressible()).toBe(true);
			
			module.exports.extension('ext2').is({mimeType:'text/html', charset:'utf-8'});
			call = support.addToMap.calls.mostRecent();
			expect(call.args[0]).toBe(name);
			expect(call.args[1]).toBe('ext2');
			expect(call.args[2].mimeType()).toEqual(valueOf('text/html'));
			expect(call.args[2].charset().name()).toEqual(valueOf('UTF-8'));
			expect(call.args[2].compressible()).toBe(false);
		});
	});
});