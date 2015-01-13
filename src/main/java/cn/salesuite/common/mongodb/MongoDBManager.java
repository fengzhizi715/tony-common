/**
 * 
 */
package cn.salesuite.common.mongodb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.salesuite.common.utils.StringUtils;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;

/**
 * @author Tony Shen
 * 
 */
public class MongoDBManager {
	
	private static Logger logger = LoggerFactory.getLogger(MongoDBManager.class);

	private static MongoClient mongoClient = null;
	private static DB db = null;
	private MongoOptions mongoOptions;
	private String dbName;
	private String hostList;

	/**
	 * key:collection 名字
	 * value:该collection对应的DBCollection
	 */
	private static Map<String,DBCollection> collObjMap = new HashMap<String,DBCollection>();

    /**
     * 初始化方法
     */
	public void init() {
		try {
			if(mongoClient == null){
				List<ServerAddress> replicaSetSeeds = new ArrayList<ServerAddress>();
				String[] hostStrs = hostList.split(",");
				for (String oneHostStr : hostStrs) {
					String[] strs = oneHostStr.split(":");
					ServerAddress serverAddr = new ServerAddress(strs[0], Integer.valueOf(strs[1]));
					replicaSetSeeds.add(serverAddr);
				}
				
				if (mongoOptions!=null) {
					mongoClient = new MongoClient(replicaSetSeeds,getMongoClientOptions(mongoOptions));
				} else {
					mongoClient = new MongoClient(replicaSetSeeds);
				}
			}
			db = mongoClient.getDB(dbName);
		} catch (Exception e) {
			logger.error("Can't connect MongoDB!");
			e.printStackTrace();
		}
	}
	
	/**
	 * 由于MongoClientOptions是基于builder模式生成的，不方便注入。所以，注入MongoOptions，通过MongoOptions来生成MongoClientOptions
	 * @param mongoOptions
	 * @return
	 */
	private MongoClientOptions getMongoClientOptions(MongoOptions mongoOptions) {
		return new MongoClientOptions.Builder()
				.connectionsPerHost(mongoOptions.connectionsPerHost)
				.connectTimeout(mongoOptions.connectTimeout)     // 链接超时时间
				.cursorFinalizerEnabled(mongoOptions.cursorFinalizerEnabled)
				.maxWaitTime(mongoOptions.maxWaitTime)
				.threadsAllowedToBlockForConnectionMultiplier(
						mongoOptions.threadsAllowedToBlockForConnectionMultiplier)
				.socketTimeout(mongoOptions.socketTimeout)       // read数据超时时间
				.socketKeepAlive(mongoOptions.socketKeepAlive)   // 是否保持长链接
				.readPreference(ReadPreference.primary())        // 最近优先策略
				.writeConcern(WriteConcern.NORMAL).build();
	}

	/**
	 * 获取集合（表）
	 * 
	 * @param collection
	 */
	public static DBCollection getCollection(String collection) {
		DBCollection dBcollection = null;
		if(!collObjMap.containsKey(collection)) {
			dBcollection = db.getCollection(collection);
			if (dBcollection!=null) {
				collObjMap.put(collection, dBcollection);
			}
		} else {
			dBcollection = collObjMap.get(collection);
		}
		
		return dBcollection;
	}

	/**
	 * 插入
	 * 
	 * @param collection
	 * @param map
	 */
	public void insert(String collection, Map<String, Object> map) {
		try {
			DBObject dbObject = map2Obj(map);
			getCollection(collection).insert(dbObject);
		} catch (MongoException e) {
			logger.error("MongoException:" + e.getMessage());
		}
	}

	/**
	 * 批量插入
	 * 
	 * @param collection
	 * @param list
	 */
	public void insertBatch(String collection, List<Map<String, Object>> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		try {
			List<DBObject> listDB = new ArrayList<DBObject>();
			for (int i = 0; i < list.size(); i++) {
				DBObject dbObject = map2Obj(list.get(i));
				listDB.add(dbObject);
			}
			getCollection(collection).insert(listDB);
		} catch (MongoException e) {
			logger.error("MongoException:" + e.getMessage());
		}
	}

	/**
	 * 删除
	 * 
	 * @param collection
	 * @param map
	 */
	public void delete(String collection, Map<String, Object> map) {
		DBObject obj = map2Obj(map);
		getCollection(collection).remove(obj);
	}

	/**
	 * 删除全部
	 * 
	 * @param collection
	 * @param map
	 */
	public void deleteAll(String collection) {
		List<DBObject> rs = findAll(collection);
		if (rs != null && !rs.isEmpty()) {
			for (int i = 0; i < rs.size(); i++) {
				getCollection(collection).remove(rs.get(i));
			}
		}
	}

	/**
	 * 批量删除
	 * 
	 * @param collection
	 * @param list
	 */
	public void deleteBatch(String collection, List<Map<String, Object>> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		for (int i = 0; i < list.size(); i++) {
			getCollection(collection).remove(map2Obj(list.get(i)));
		}
	}

	/**
	 * 计算满足条件条数
	 * 
	 * @param collection
	 * @param map
	 */
	public long getCount(String collection, Map<String, Object> map) {
		return getCollection(collection).getCount(map2Obj(map));
	}

	/**
	 * 计算集合总条数
	 * 
	 * @param collection
	 * @param map
	 */
	public long getCount(String collection) {
		return getCollection(collection).find().count();
	}

	/**
	 * 更新
	 * 
	 * @param collection
	 * @param query 查询条件相当于sql语句中的where
	 * @param update 更新条件相当于sql语句中的set
	 */
	public void update(String collection, Map<String, Object> query,
			Map<String, Object> update) {
		DBObject queryObject = map2Obj(query);
		DBObject updateObject = map2Obj(update);
		
		DBObject updateSetValue=new BasicDBObject("$set",updateObject);  
		getCollection(collection).update(queryObject,updateSetValue);
	}
	
	/**
	 * 批量更新
	 * 
	 * @param collection
	 * @param query 查询条件相当于sql语句中的where
	 * @param update 更新条件相当于sql语句中的set
	 */
	public void updateMulti(String collection, Map<String, Object> query,
			Map<String, Object> update) {
		DBObject queryObject = map2Obj(query);
		DBObject updateObject = map2Obj(update);
		
		DBObject updateSetValue=new BasicDBObject("$set",updateObject);  
		getCollection(collection).updateMulti(queryObject,updateSetValue);
	}

	/**
	 * 查找对象（根据主键_id）
	 * 
	 * @param collection
	 * @param _id
	 */
	public DBObject findById(String collection, String _id) {
		DBObject obj = new BasicDBObject();
		obj.put("_id", ObjectId.massageToObjectId(_id));
		return getCollection(collection).findOne(obj);
	}

	/**
	 * 查找集合所有对象
	 * 
	 * @param collection
	 */
	public List<DBObject> findAll(String collection) {
		return getCollection(collection).find().toArray();
	}
	
	/**
	 * 查找集合所有对象
	 * @param collection
	 * @param sort
	 * @param limit
	 * @return
	 */
	public List<DBObject> findAll(String collection,DBObject sort,int limit) {
		return getCollection(collection).find().limit(limit).sort(sort).toArray();
	}
	
	/**
	 * 查找集合所有对象
	 * @param collection
	 * @param query
	 * @param sort
	 * @param limit
	 * @return
	 */
	public List<DBObject> findAll(String collection,Map<String, Object> query,DBObject sort,int limit) {
		return getCollection(collection).find(map2Obj(query)).limit(limit).sort(sort).toArray();
	}
	
	public List<DBObject> findAll(String collection,DBObject dBObject) {
		return getCollection(collection).find(dBObject).toArray();
	}
	
	public List<DBObject> findAll(String collection,Map<String, Object> query) {
		return getCollection(collection).find(map2Obj(query)).toArray();
	}

	/**
	 * 查找（返回一个对象）
	 * 
	 * @param map
	 * @param collection
	 */
	public DBObject findOne(String collection, Map<String, Object> map) {
		DBCollection coll = getCollection(collection);
		return coll.findOne(map2Obj(map));
	}

	/**
	 * 查找（返回一个List<DBObject>）
	 * 
	 * @param <DBObject>
	 * @param map
	 * @param collection
	 * @throws Exception
	 */
	public List<DBObject> find(String collection, Map<String, Object> map)
			throws Exception {
		DBCollection coll = getCollection(collection);
		DBCursor c = coll.find(map2Obj(map));
		if (c != null)
			return c.toArray();
		else
			return null;
	}
	
	public BasicDBList group(String collection, String[] keyColumn, DBObject condition,
            DBObject initial, String reduce, String finalize) {
        DBCollection coll = getCollection(collection);
        DBObject key = new BasicDBObject();
        for (int i = 0; i < keyColumn.length; i++) {
            key.put(keyColumn[i], true);
        }
        condition = (condition == null) ? new BasicDBObject() : condition;
        if (StringUtils.isEmpty(finalize)) {
            finalize = null;
        }
        
        initial = (initial == null) ? new BasicDBObject() : initial;
        BasicDBList resultList = (BasicDBList) coll.group(key, condition,
                initial, reduce, finalize);
        return resultList;
    }
	
	/**
	 * 插入一个文件
	 * 
	 * @param filePath
	 * @param fileName
	 */
	public void insertFile(String filePath, String fileName) {
		insertFile("fs",filePath,fileName);
	}
	
	/**
	 * 插入一个文件
	 * 
	 * @param bucket
	 * @param filePath
	 * @param fileName
	 */
	public void insertFile(String bucket, String filePath, String fileName) {
		GridFS fs = new GridFS(db, bucket);
		File file = new File(filePath);
		
		try {
			InputStream input = new FileInputStream(file);
			GridFSInputFile fsFile = fs.createFile(input);
			fsFile.setFilename(fileName);
			fsFile.save();
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public InputStream getInputStream(String fileName) {
		return getInputStream("fs",fileName);
	}
	
	public InputStream getInputStream(String bucket,String fileName) {
		GridFS fs = new GridFS(db,bucket);
		return fs.findOne(fileName).getInputStream();
	}
	
	/**
	 * 根据文件名删除文件
	 * @param fileName
	 */
	public void removeFile(String fileName) {
		GridFS gfs = new GridFS(db, "fs");
		gfs.remove(fileName);
	}
	
	/**
	 * 根据文件名删除文件
	 * @param bucket
	 * @param fileName
	 */
	public void remove(String bucket, String fileName) {
		GridFS gfs = new GridFS(db, bucket);
		gfs.remove(fileName);
	}
	   
	private DBObject map2Obj(Map<String, Object> map) {
		DBObject object = new BasicDBObject();
		for (String key : map.keySet()) {
			object.put(key, map.get(key));
		}
		return object;
	}
	
	// getter、setter()
	public MongoOptions getMongoOptions() {
		return mongoOptions;
	}

	public void setMongoOptions(MongoOptions mongoOptions) {
		this.mongoOptions = mongoOptions;
	}
	
	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	
	public String getHostList() {
		return hostList;
	}

	public void setHostList(String hostList) {
		this.hostList = hostList;
	}
}
