// just for testing

module.exports = function() {
	Array.prototype.forEach.call(arguments, function(arg) {
		java.lang.System.out.append(java.lang.String.valueOf(arg)).append(" ");
	});
	java.lang.System.out.println();
}