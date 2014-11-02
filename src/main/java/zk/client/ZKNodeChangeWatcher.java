package zk.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;

import zk.utils.Constants;
import zk.utils.PropertyLoader;

/**
 *  This class checks for any file changes and accordingly gets updates.
 */
public class ZKNodeChangeWatcher implements Watcher {

	/** The zoo keeper. */
	private ZooKeeper zooKeeper;

	/** The Constant TIMEOUT. */
	private static int TIMEOUT;

	/** The Constant ZNODE. */
	private static String ZNODE;

	/** The latch. */
	private CountDownLatch latch = new CountDownLatch(1);

	/** The Constant PROP_FILE. */
	private static File PROP_FILE;

	/** The Constant HOST. */
	private static String HOST;

	/**
	 * Default constructor.
	 */
	public ZKNodeChangeWatcher() { }

	public void init(){
		HOST = PropertyLoader.getPropertyValue(Constants.HOST);
		ZNODE = PropertyLoader.getPropertyValue(Constants.ZNODE);
		PROP_FILE = new File(PropertyLoader.getPropertyValue(Constants.FILE_PATH));
		TIMEOUT = Integer.parseInt(PropertyLoader.getPropertyValue(Constants.NODE_CHANGE_TIMEOUT));
	}

	/**
	 * This method creates the ZooKeeper connection.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 * @throws KeeperException the keeper exception
	 * @see ZooKeeper
	 */
	final void startZK() throws IOException, InterruptedException, KeeperException {
		zooKeeper = new ZooKeeper(HOST, TIMEOUT, this);
		latch.await();
	}

	/**
	 * Given method returns the instance of <code>ZooKeeper</code>.
	 * @return <code>ZooKeeper</code>
	 * @see ZooKeeper
	 */
	final ZooKeeper getZooKeeper(){
		// Verify ZooKeeper's validity
		if (null == zooKeeper || !zooKeeper.getState().equals(States.CONNECTED)){
			throw new IllegalStateException ("ZooKeeper is not connected.");
		}
		return zooKeeper;
	}

	/* 
	 * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.WatchedEvent)
	 */
	@Override
	public final void process(final WatchedEvent event) {
		System.out.println(event.getState());

		if (event.getState() == Event.KeeperState.SyncConnected) {
			latch.countDown();
		}

		if(event.getType() == Event.EventType.NodeDataChanged) {
			if(updateFile()){
				System.out.println("File Updated");
			} else {
				System.out.println("File Could Not Be Updated");
			}
		}
	}

	/**
	 * This methods updates the specified properties files by looking up to the Node if it exists.
	 *
	 * @return true, if successful
	 */
	protected final boolean updateFile(){
		byte[] nodeData = null;
		String sNodeData = null;
		PrintWriter writer = null;
		boolean retVal = true;
		try {
			nodeData = zooKeeper.getData(ZNODE, false, null);
			if(nodeData != null && nodeData.length > 0){
				sNodeData = new String(nodeData);
				if(sNodeData != null && sNodeData.length() > 0){
					writer = new PrintWriter(PROP_FILE);
					writer.write(sNodeData);
				}
			}

		} catch (KeeperException | InterruptedException | FileNotFoundException e) {
			e.printStackTrace();
			retVal = false;
		} finally {
			if(writer != null){
				writer.flush();
				writer.close();
			}
		}
		return retVal;
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 * @throws KeeperException the keeper exception
	 */
	public static void main(final String[] args) {
		ZKNodeChangeWatcher watcher = new ZKNodeChangeWatcher();
		watcher.init();
		ZooKeeper zk = null;

		try {
			watcher.startZK();
			zk = watcher.getZooKeeper();

			while(true){
				zk.getData(ZNODE, true, null);
				Thread.sleep(TIMEOUT);
			}

		} catch (IOException | InterruptedException | KeeperException e) {
			e.printStackTrace();
		}
	}
}
