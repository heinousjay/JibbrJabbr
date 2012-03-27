package fun;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class Pointless {
	
	private static final int granularity = 20;
	
	private static final ExecutorService es = Executors.newFixedThreadPool(granularity);
	
	public static void main(String[] args) 
		throws InterruptedException, ExecutionException {
		
		Future<Long> result = es.submit(new FileWalkingTest());
		
		System.out.printf("\nResult = %d\n", result.get());
		es.shutdown();
	}
	
	static class FileWalkingTest extends SimpleFileVisitor<Path> implements Callable<Long> {

		private final List<Future<Long>> results = new ArrayList<>();
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			results.add(es.submit(new FileReadingTest(file)));
			return CONTINUE;
		}
		
		@Override
		public Long call() {
			
			System.out.println("HERE WE GO!");
			
			try {
				FileSystem fs = FileSystems.getDefault();
				Files.walkFileTree(fs.getPath("/Users/jason/Downloads"), this);
				System.out.println("totalling");
				long total = 0;
				for (Future<Long> result : results) {
					long answer = result.get();
					total += answer;
					System.out.printf("%d - %d\n", answer, total);
				}
				System.out.printf("\n\nTHERE SHOULD BE ME!\n%d\n", total);
				return total;
				
			} catch (IOException | InterruptedException | ExecutionException e) {
				System.err.println("NOSIR");
				e.printStackTrace();
				return 0L;
			}
		}
		
		
	}
	
	// reads a file and counts the bytes
	static class FileReadingTest implements Callable<Long> {

		static int count = 0;
		
		private final Path path;
		
		FileReadingTest(final Path path) {
			this.path = path;
		}
		
		@Override
		public Long call() {
			System.out.print('.');
			if (++count % granularity == 0) System.out.println();
			long result;
			try {
				result = Files.readAttributes(path, BasicFileAttributes.class).size();
				if (count % 50 == 0) System.err.println(result);
				return result;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return 0L;
			}
		}
		
	}
}
