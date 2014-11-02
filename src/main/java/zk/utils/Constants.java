package zk.utils;

public interface Constants {

	/** Config file path for the ZooKeeper client. */
	String CONF_FILE_PATH = "C:/ZK_Resources/conf.properties";
	
	/** Location of the File to be modified. */
	String FILE_PATH = "FILE_PATH";
	
	/** Machine name with Port No. of the hosted ZooKeeper server. */
	String HOST = "HOST";
	
	/** Node to be Watched on ZooKeeper server. */
	String ZNODE = "ZNODE";
	
	/** Node change Watcher TimeOut (in seconds). */
	String NODE_CHANGE_TIMEOUT = "NODE_CHANGE_TIMEOUT";
	
	/** File change Watcher TimeOut (in seconds). */
	String FILE_CHANGE_TIMEOUT = "FILE_CHANGE_TIMEOUT";
	
	/** The Constant LINE_SEPARATOR. */
	String LINE_SEPARATOR = "\n";
}
