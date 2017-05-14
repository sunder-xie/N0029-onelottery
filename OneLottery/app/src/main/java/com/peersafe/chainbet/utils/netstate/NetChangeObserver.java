
/**   
 * Copyright (C) PeerSafe 2015 Technologies. All rights reserved.
 *
 * @Name: NetChangeObserver.java 
 * @Package: com.peersafe.shadowtalk.utils.netstate 
 * @Description: 网路状态改变的观察者
 * @author zhangyang  
 * @date 2015年6月17日 下午2:22:18 
 */


package com.peersafe.chainbet.utils.netstate;


/** 
 * @Description 
 * @author zhangyang
 * @date 2015年6月17日 下午2:22:18 
 */

public interface NetChangeObserver
{
    /** 
     * @Description 网络连接连接时调用
     * @author zhangyang
     * @param type  
     */
    public void onConnect(int type);

    /** 
     * @Description 
     * @author zhangyang  
     */
    public void onDisConnect();
}
