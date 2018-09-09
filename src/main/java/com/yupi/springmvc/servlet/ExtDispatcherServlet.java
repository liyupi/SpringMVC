package com.yupi.springmvc.servlet;

import com.github.lordrex34.reflection.util.ClassPathUtil;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.yupi.springmvc.annotation.ExtController;
import com.yupi.springmvc.annotation.ExtRequestMapping;
import org.apache.commons.lang3.ClassPathUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 功能描述：自定义前端控制器
 *
 * @author Yupi Li
 * @date 2018/9/1 22:01
 */
public class ExtDispatcherServlet extends HttpServlet {

    // SpringMVC Bean容器
    private ConcurrentHashMap<String, Object> springmvcBeans = new ConcurrentHashMap<>();
    // url和bean映射
    private ConcurrentHashMap<String, Object> urlBeans = new ConcurrentHashMap<>();
    // url和方法名映射，实际第二个参数应为map<String,List<>>（方法名称和参数列表）集合
    private ConcurrentHashMap<String, String> urlMethods = new ConcurrentHashMap<>();


    @Override
    public void init() throws ServletException {
        // 获取指定包下所有类
        try {
            // getAllClasses扫包工具类一定要带classloader参数
            FluentIterable<Class<?>> allClasses = ClassPathUtil.getAllClasses(this.getClass().getClassLoader(), "com.yupi.controller");
            ImmutableList<Class<?>> classes = allClasses.toList();
            findClassMVCAnnotation(classes);
            findClassAndMethodRequestMapping();
        } catch (IOException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 获取请求url地址
        String url = req.getRequestURI();
        if (StringUtils.isEmpty(url)) {
            return;
        }
        // 从urlBeans容器中获取对象
        Object object = urlBeans.get(url);
        if (object == null) {
            resp.getWriter().println("404 Not Found!");
            return;
        }
        // 使用地址获取方法
        String methodName = urlMethods.get(url);
        if (StringUtils.isEmpty(methodName)) {
            resp.getWriter().println("Not Found Method");
            return;
        }
        // 调用方法
        try {
            String resultPage = methodInvoke(object, methodName);
            // 返回字符串
//            resp.getWriter().println(resultPage);
            // 返回页面
            extResourceViewResolver(resultPage,req,resp);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 调用方法
     */
    private String methodInvoke(Object object, String methodName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> classInfo = object.getClass();
        Method method = classInfo.getMethod(methodName);
        String result = (String) method.invoke(object);
        return result;
    }

    /**
     * 将带有ExtController注解的bean注入SpringMVC容器
     */
    private void findClassMVCAnnotation(List<Class<?>> classList) throws IllegalAccessException, InstantiationException {
        for (Class<?> aClass : classList) {
            // 判断当前类是否有ExtController注解
            ExtController extController = aClass.getAnnotation(ExtController.class);
            if (extController != null) {
                String beanId = StringUtils.uncapitalize(aClass.getSimpleName());
                Object object = aClass.newInstance();
                springmvcBeans.put(beanId, object);
            }
        }
    }

    /**
     * 将url映射和类方法关联
     */
    public void findClassAndMethodRequestMapping() {
        // 遍历springMVCBean容器，判断类上是否有RequestMapping注解
        for (Map.Entry<String, Object> entry : springmvcBeans.entrySet()) {
            Object object = entry.getValue();
            // 判断类上是否有RequestMapping注解
            ExtRequestMapping requestMapping = object.getClass().getDeclaredAnnotation(ExtRequestMapping.class);
            String url = "";
            if (requestMapping != null) {
                url = requestMapping.value();
            }
            // 获取类上所有方法
            Method[] methods = object.getClass().getDeclaredMethods();
            for (Method method : methods) {
                // 判断方法上是否有RequestMapping注解
                ExtRequestMapping methodRequestMapping = method.getDeclaredAnnotation(ExtRequestMapping.class);
                if (methodRequestMapping != null) {
                    String methodUrl = url + methodRequestMapping.value();
                    urlBeans.put(methodUrl, object);
                    urlMethods.put(methodUrl, method.getName());
                }
            }
        }
    }

    // 视图解析器
    private void extResourceViewResolver(String pageName, HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        // 配置前后缀
        String prefix = "/";
        String suffix = ".jsp";
        request.getRequestDispatcher(prefix + pageName + suffix).forward(request, resp);
    }

}
