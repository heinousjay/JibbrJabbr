
const ro = inject('jj.http.client.api.RestOperation');

const GET = Packages.io.netty.handler.codec.http.HttpMethod.GET;

ro.request(GET, 'http://localhost:8080/test.txt', {});