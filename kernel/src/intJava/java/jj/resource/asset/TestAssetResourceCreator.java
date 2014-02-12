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
package jj.resource.asset;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import jj.resource.asset.AssetResourceCreator;

/**
 * @author jason
 *
 */
public class TestAssetResourceCreator extends AssetResourceCreator {

	
	/**
	 * @param instanceModuleCreator
	 */
	public TestAssetResourceCreator() {
		super(null);
	}

	public byte[] toBytes(final String baseName) throws IOException {
		if (Asset.jar == null) {
			return Files.readAllBytes(Asset.appPath.resolve(baseName));
		} else {
			try (FileSystem myJarFS = FileSystems.newFileSystem(Asset.jar, null)) {
				return Files.readAllBytes(myJarFS.getPath("/jj/assets", baseName));
			}
		}
	}
}
