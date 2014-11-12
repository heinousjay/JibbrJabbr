var support = require('jj/configuration-support')('jj.http.client.HttpClientConfiguration');

function validateIpAddress(name, address) {
	if (typeof address !== 'string') {
		return support.accumulateError(name, " must be a string");
	}
}

module.exports = {
	localClientAddress: support.makeStringProperty('localClientAddress', validateIpAddress),
	localNameserverAddress: support.makeStringProperty('localNameserverAddress', validateIpAddress),
	nameserver: support.makeAddToList('nameservers', validateIpAddress)
}