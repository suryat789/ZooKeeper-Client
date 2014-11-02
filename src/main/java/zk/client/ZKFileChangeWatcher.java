package zk.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import zk.utils.Constants;
import zk.utils.PropertyLoader;

/**
 * <code>ZKFileChangeWatcher</code> updates the Node value on the server if the properties files defined is updated.
 */
public class ZKFileChangeWatcher implements Watcher {

	/** The Property File. */
	private static File PROP_FILE;

	/** The last modified. */
	private long lastModified = 0;

	/** The zookeeper instance. */
	private ZooKeeper zooKeeper;

	/** The Constant ZNODE. */
	private static String ZNODE;

	/** The Constant TIMEOUT. */
	private static int TIMEOUT;

	/** The Constant latch. */
	private static final CountDownLatch latch = new CountDownLatch(1);

	/** The Constant HOST. */
	private static String HOST;

	/**
	 * Default constructor.
	 */
	public ZKFileChangeWatcher() {}

	public void init(){
		HOST = PropertyLoader.getPropertyValue(Constants.HOST);
		ZNODE = PropertyLoader.getPropertyValue(Constants.ZNODE);
		PROP_FILE = new File(PropertyLoader.getPropertyValue(Constants.FILE_PATH));
		TIMEOUT = Integer.parseInt(PropertyLoader.getPropertyValue(Constants.FILE_CHANGE_TIMEOUT));
	}

	/**
	 * This method creates the ZooKeeper connection.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 * @see ZooKeeper
	 */
	public final void startZK() throws IOException, InterruptedException  {
		zooKeeper = new ZooKeeper(HOST, TIMEOUT,this);
		latch.await();
	}

	/* 
	 * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.WatchedEvent)
	 */
	@Override
	public void process(final WatchedEvent event) {
		if (event.getState() == Event.KeeperState.SyncConnected) {
			latch.countDown();
		}
	}

	/**
	 * This method checks if the file is modified and writes the data to the specified node on the server. 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	protected final void updateNodeData() throws IOException, InterruptedException{
		byte[] nodeData = null;
		String sNodeData = null;

		while (true) {
			if (lastModified < PROP_FILE.lastModified()) {
				System.out.println("Last modified timestamp changed.");
				lastModified = PROP_FILE.lastModified();
				//Read the file and notify zookeeper.
				BufferedReader br = null;
				StringBuffer buffer = new StringBuffer(100);
				try {
					br = new BufferedReader(new FileReader(PROP_FILE));
					String line ="";
					while((line = br.readLine())!= null) {
						buffer.append(line).append(Constants.LINE_SEPARATOR);
					}
					
					//Persist changes into ZK node
					Stat s = zooKeeper.exists(ZNODE,false);

					nodeData = zooKeeper.getData(ZNODE, false, null);


					if (s != null) {
						zooKeeper.setData(ZNODE, buffer.toString().getBytes(), -1);
					}
					nodeData = zooKeeper.getData(ZNODE, false, null);
					sNodeData = new String(nodeData);

				} catch (FileNotFoundException | KeeperException e) {
					e.printStackTrace();
				} finally {
					try {
						if(br != null)
							br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else {
				System.out.println("File not changed.");
			}
			Thread.sleep(5 * 1000); //check every 5 seconds.
		}
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	public static void main(final String[] args) throws IOException, InterruptedException {
		ZKFileChangeWatcher watcher = new ZKFileChangeWatcher();
		watcher.init();
		watcher.startZK();
		watcher.updateNodeData();
	}
}
