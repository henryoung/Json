package jsonparseutil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;

public class ConcurrentStreamingParser
{
	private String jsonDataFilename;
	private Map<Long, Future<JsonParseResult>> results;

	public ConcurrentStreamingParser(String fileName)
	{
		jsonDataFilename = fileName;
	}

	public void Parse()
	{
		try
		{
			Map<Long, Long> posMap = splitJsonFile(jsonDataFilename);

			Set<Long> setKey = posMap.keySet();

			ExecutorService executor = Executors.newCachedThreadPool();
			results = new TreeMap<Long, Future<JsonParseResult>>();
			for (long startPos : setKey)
			{
				long endPos = posMap.get(startPos);
				results.put(startPos, executor.submit(new CallableJsonParseThread(jsonDataFilename, startPos, endPos)));
			}
			executor.shutdown();

			try
			{
				while (!executor.awaitTermination(500, TimeUnit.MICROSECONDS))
					;
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}

			Set<Long> resKey = results.keySet();
			File file = new File("xxx.json");
			file.delete();
			int keySize = resKey.size();
			int index = 0;
			for (long key : resKey)
			{
				try
				{
					JsonParseResult jpr = results.get(key).get();
					if (!jpr.jObjList.isEmpty())
					{
						//System.out.println(jpr.jObjList);
					}

					ByteBuffer bf = jpr.outStream.buf;
					int slicePos = (int) jpr.outStream.writeLength;
					bf.limit(slicePos);
					if (index != 0)
					{
						bf.put(0, (byte) ',');

					}
					if (index != keySize - 1)
					{
						bf.put(slicePos - 1, (byte) ' ');
					}

					boolean append = true;
					FileOutputStream fs = new FileOutputStream(file, append);
					FileChannel wChannel = fs.getChannel();

					wChannel.write(bf);

					wChannel.close();
					fs.close();

				} catch (InterruptedException e)
				{
					e.printStackTrace();
				} catch (ExecutionException e)
				{
					e.printStackTrace();
				}
				++index;
			}

		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private Map<Long, Long> splitJsonFile(String fileName) throws IOException
	{

		RandomAccessFile file = new RandomAccessFile(fileName, "r");
		MappedByteBuffer mbBuf = file.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());

		ArrayList<Long> positionList = new ArrayList<Long>();
		Map<Long, Long> splitMap = new TreeMap<Long, Long>();

		long partialLength = (long) 1024 * 1024 * 4;

		long fileLength = file.length();

		if (partialLength >= fileLength)
		{
			positionList.add(fileLength - 1);
			splitMap.put((long) 0, fileLength - 1);
			file.close();
			return splitMap;
		}

		long curPos = partialLength;
		while (curPos < (fileLength - 1))
		{
			char c = (char) mbBuf.get((int) curPos);
			if (c == ']')
			{
				positionList.add(curPos);
				curPos = (curPos + partialLength) < (fileLength - 1) ? (curPos + partialLength) : (fileLength - 1);
			} else
			{
				++curPos;
			}
		}

		file.close();

		positionList.add(fileLength - 2);

		int size = positionList.size();

		splitMap.put((long) 0, positionList.get(0) + 1);

		for (int i = 0; i < size - 1; ++i)
		{
			splitMap.put(positionList.get(i) + 1, positionList.get(i + 1) + 1);
		}

		return splitMap;
	}

}

class JsonParseResult
{
	ByteBufferBackedOutputStream outStream;
	ArrayList<JsonObject> jObjList;

}

class CallableJsonParseThread implements Callable<JsonParseResult>
{

	private MappedByteBuffer mbBuf;
	private ByteBuffer outBuf;
	private InputStreamReader in;
	private OutputStreamWriter out;
	private RandomAccessFile file;
	private ByteBufferBackedOutputStream outStream;
	private JsonParseResult jpr;

	public CallableJsonParseThread(String fileName, long start, long end)
	{
		jpr = new JsonParseResult();
		try
		{
			file = new RandomAccessFile(fileName, "rw");
			mbBuf = file.getChannel().map(FileChannel.MapMode.PRIVATE, start, end - start + 1);

			if (mbBuf.get(0) != (byte) '{')
			{
				mbBuf.put(0, (byte) '{');
				mbBuf.position(0);
			}
			mbBuf.put(mbBuf.capacity() - 1, (byte) '}');

			in = new InputStreamReader(new ByteBufferBackedInputStream(mbBuf));
			// out = new OutputStreamWriter(System.out, "UTF-8");
			outBuf = ByteBuffer.allocate((int) (end - start) * 2);
			outStream = new ByteBufferBackedOutputStream(outBuf);
			out = new OutputStreamWriter(outStream);

		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public JsonParseResult call() throws Exception
	{

		MatchRule test = new MatchRule();
		test.setPARTN_ROLE("AG");
		test.setSD_DOC("0000000151");
		AbstractMap.SimpleImmutableEntry<String, Integer> pair = new AbstractMap.SimpleImmutableEntry<>("LEVEL_NR",
				100);
		ArrayList<JsonObject> jObjList = JsonParseExUtilities.GsonStreamParse(in, out, "ORDER_PARTNERS_OUT", test,
				pair);
		in.close();
		file.close();
		jpr.jObjList = jObjList;
		outStream.buf.position(0);
		jpr.outStream = outStream;
		return jpr;
	}
}

class ByteBufferBackedInputStream extends InputStream
{

	MappedByteBuffer buf;

	ByteBufferBackedInputStream(MappedByteBuffer buf)
	{
		this.buf = buf;
	}

	public synchronized int read() throws IOException
	{
		if (!buf.hasRemaining())
		{
			return -1;
		}
		return buf.get();
	}

	public synchronized int read(byte[] bytes, int off, int len) throws IOException
	{
		len = Math.min(len, buf.remaining());
		buf.get(bytes, off, len);
		return len;
	}
}

class ByteBufferBackedOutputStream extends OutputStream
{
	ByteBuffer buf;

	long writeLength;

	ByteBufferBackedOutputStream(ByteBuffer buf)
	{
		this.buf = buf;
	}

	public synchronized void write(int b) throws IOException
	{
		buf.put((byte) b);
		++writeLength;
	}

	public synchronized void write(byte[] bytes, int off, int len) throws IOException
	{
		buf.put(bytes, off, len);
		writeLength += len;
	}

}
