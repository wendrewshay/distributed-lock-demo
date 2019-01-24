package com.xwq.distributedlockzookeeper.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 分布式锁实现
 * @author by Joney on 2019/1/24 16:33
 */
public class SimpleDistributedLock extends DistributedLock {

    private static ZooKeeper zkClient = null;

    private String rootPath = null;

    private String lockPath = null;

    private String lockName = null;

    private String competitorPath = null;

    private String thisCompetitorPath = null;

    private String waitCompetitorPath = null;

    /**
     * 锁在zk中的根节点
     */
    private final static String ROOT_LOCK_NODE = "/locks";

    /**
     * 竞争者节点，每个想要尝试获取锁的节点都会获得一个竞争者节点
     */
    private static final String COMPETITOR_NODE = "competitorNode";
    /**
     * 锁默认的临时节点的超时时间，单位毫秒
     */
    private static final int DEFAULT_SESSION_TIMEOUT = 5000;

    /**
     * 与zookeeper连接成功后消除栅栏
     */
    private CountDownLatch latch = new CountDownLatch(1);
    private CountDownLatch getLockLatch = new CountDownLatch(1);

    /**
     * 尝试获得锁，一直阻塞，直到获得锁为止
     *
     * @return
     */
    @Override
    public void acquire() {
        if (StringUtils.isEmpty(rootPath) || StringUtils.isEmpty(lockName) || zkClient == null) {
            throw new LockException("未初始化zookeeper连接");
        }
        List<String> allCompetitorList = null;
        try {
            createCompetitorNode();
            allCompetitorList = zkClient.getChildren(lockPath, false);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Collections.sort(allCompetitorList);
        int index = allCompetitorList.indexOf(thisCompetitorPath.substring((lockPath+"/").length()));
        if (index == -1) {
            throw new LockException("competitorPath not exist after create.");
        } else if (index == 0) {
            return;
        } else {
            waitCompetitorPath = lockPath + "/" + allCompetitorList.get(index - 1);
            Stat waitNodeStat;
            try {
                waitNodeStat = zkClient.exists(waitCompetitorPath, event -> {
                    if (event.getType().equals(Watcher.Event.EventType.NodeDeleted) && event.getPath().equals(waitCompetitorPath)) {
                        getLockLatch.countDown();
                    }
                });
                if (waitNodeStat == null) {
                    return;
                } else {
                    getLockLatch.await();
                    return;
                }
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 尝试获得锁，如果不能获得也立即返回
     */
    @Override
    public boolean tryAcquire() {
        if (StringUtils.isEmpty(rootPath) || StringUtils.isEmpty(lockName) || zkClient == null) {
            throw new LockException("未初始化zookeeper连接");
        }
        List<String> allCompetitorList = null;
        try {
            createCompetitorNode();
            allCompetitorList = zkClient.getChildren(lockPath, false);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Collections.sort(allCompetitorList);
        int index = allCompetitorList.indexOf(thisCompetitorPath.substring((lockPath + "/").length()));
        if (index == -1) {
            throw new LockException("competitorPath not exist after create.");
        } else if (index == 0) { // 如果发现自己是最小节点，那么说明获得了锁
            return true;
        } else { // 此处说明不是最小节点
            return false;
        }
    }

    /**
     * 在有效期时间内尝试获取锁，如果不能获得也立即返回
     *
     * @param timeout
     * @return
     */
    @Override
    public boolean tryAcquire(int timeout) {
        return false;
    }

    /**
     * 释放锁
     */
    @Override
    public void release() {

    }

    /**
     * 创建竞争者节点
     * @author by Joney on 2019/1/24 16:42
     */
    private void createCompetitorNode() throws KeeperException, InterruptedException {
        competitorPath = lockPath + "/" + COMPETITOR_NODE;
        thisCompetitorPath = zkClient.create(competitorPath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    /**
     * 连接zookeeper
     * @param zkhosts  连接信息
     * @param lockName 锁名称
     * @author by Joney on 2019/1/24 17:01
     */
    public void connectZooKeeper(String zkhosts, String lockName) throws IOException, InterruptedException, KeeperException {
        if (StringUtils.isEmpty(zkhosts)) {
            throw new LockException("zookeeper hosts cannot be empty.");
        }
        if (StringUtils.isEmpty(lockName)) {
            throw new LockException("lockName cannot be empty.");
        }
        // 连接zookeeper
        if (zkClient == null) {
            zkClient = new ZooKeeper(zkhosts, DEFAULT_SESSION_TIMEOUT, event -> {
                if (event.getState().equals(Watcher.Event.KeeperState.SyncConnected)) {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // 判断锁的根节点是否存在，不存在则创建
        Stat rootStat = zkClient.exists(ROOT_LOCK_NODE, false);
        if (rootStat == null) {
            rootPath = zkClient.create(ROOT_LOCK_NODE, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } else {
            rootPath = ROOT_LOCK_NODE;
        }
        // 判断相应锁节点是否存在，不存在则创建
        String lockNodePath = ROOT_LOCK_NODE + "/" + lockName;
        Stat lockStat = zkClient.exists(lockNodePath, false);
        if (lockStat == null) {
            lockPath = zkClient.create(lockNodePath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } else {
            lockPath = lockNodePath;
        }
    }
}
