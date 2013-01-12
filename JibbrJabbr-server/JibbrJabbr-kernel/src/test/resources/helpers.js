

// commonjs module, so only what's assigned to exports is going to be exposed

var amp = /&/g;
var quot = /"/g;
var lt = /</g;
exports.dehtml = function(input) {
	return input.replace(amp, '&amp;').replace(lt, '&lt;').replace(quot, '&quot;');
};

// the hostapi works in here.
// although details will be changing shortly
// module.id refers to the identifier of this module, suitable for
// passing to require
print(module.id);