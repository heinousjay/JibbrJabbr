package jj.resource;

public enum ScriptResourceType {
	Client {
		public String suffix() {
			return ".js";
		}
	},
	Shared {
		public String suffix() {
			return ".shared.js";
		}
	},
	Server {
		public String suffix() {
			return ".server.js";
		}
	};
	
	public abstract String suffix();
}