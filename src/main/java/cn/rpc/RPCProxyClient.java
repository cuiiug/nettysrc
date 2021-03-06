package cn.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RPCProxyClient implements InvocationHandler {

	private Object obj;

	public RPCProxyClient(Object obj) {
		this.obj = obj;
	}

	// 得到被代理的对象
	public static Object getProxy(Object obj) {
		return Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(), new RPCProxyClient(obj));
	}

	// 调用此方法
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// 结果参数
		Object result = new Object();
		//
		//
		return result;
	}

}
