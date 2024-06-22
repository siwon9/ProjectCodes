package org.choongang.global.config.containers;

import org.choongang.global.config.annotations.Component;
import org.choongang.global.config.annotations.Controller;
import org.choongang.global.config.annotations.RestController;
import org.choongang.global.config.annotations.Service;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanContainer {
    private static BeanContainer instance;

    private Map<String, Object> beans;


    public BeanContainer() {
        beans = new HashMap<>();
    }

    public void loadBeans() {
        // 패키지 경로 기준으로 스캔 파일 경로 조회
        try {
            String rootPath = new File(getClass().getResource("../../../").getPath()).getCanonicalPath();
            // 클래스 클래스 정보를 가져와서 , ../../../ 의 세단계 주소를 가져온다, getPath로 문자열로 반환한걸
            // 문자열 형식을 가진 파일으로 만들어서,(File 객체 생성)
            // getCanonicalPath로 파일 시스템 규칙에 맞게 정리된 절대 경로로 변환합니다.

            // 즉 클래스의 경로를 가져와서 규칙에 맞게 정리된 절대경로로 변환하는 파일을 만들어 rootPath에 대입.

            String packageName = getClass().getPackageName().replace(".global.config.containers", "");
            // 클래스클래스 정보 가져와서, 클래스가 속한 패키지 이름을 가져와서, target의 주소를 replacment처럼 바꾼 후 packageName에 대입
            // 예를 들면 .global.config.container.myStudy가있으면 .myStudy로 변환해서 문자열로 대입

            List<Class> classNames = getClassNames(rootPath, packageName);


            for (Class clazz : classNames) {
                // 인터페이스는 동적 객체 생성을 하지 않으므로 건너띄기
                if (clazz.isInterface()) {
                    continue;
                }

                // 애노테이션 중 Controller, RestController, Component, Service 등이 TYPE 애노테이션으로 정의된 경우 beans 컨테이너에 객체 생성하여 보관
                // 키값은 전체 클래스명, 값은 생성된 객체
                String key = clazz.getName();

                // 이미 생성된 객체라면 생성된 객체로 활용
                if (beans.containsKey(key)) continue;;


                Annotation[] annotations = clazz.getDeclaredAnnotations();

                boolean isBean = false;
                for (Annotation anno : annotations) {
                    if (anno instanceof Controller || anno instanceof RestController || anno instanceof Service || anno instanceof Component)  {
                        isBean = true;
                        break;
                    }
                }
                // 컨테이너가 관리할 객체라면 생성자 매개변수의 의존성을 체크하고 의존성이 있다면 해당 객체를 생성하고 의존성을 해결한다.
                if (isBean) {
                    Constructor con = clazz.getDeclaredConstructors()[0];
                    List<Object> objs = resolveDependencies(key, con);
                    if (!beans.containsKey(key)) {
                        Object obj = con.getParameterTypes().length == 0 ? con.newInstance() : con.newInstance(objs.toArray());
                        beans.put(key, obj);
                    }
                }

            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BeanContainer getInstance() {
        if (instance == null) {
            instance = new BeanContainer();
        }

        return instance;
    }

    /**
     * 생성된 객체 조회
     *
     * @param clazz
     * @return
     */
    public <T> T getBean(Class clazz) {
        return (T)beans.get(clazz.getName());
    }

    public void addBean(Object obj) {

        beans.put(obj.getClass().getName(), obj);
    }

    public void addBean(String key, Object obj) {
        beans.put(key, obj);
    }

    // 전체 컨테이너 객체 반환
    public Map<String, Object> getBeans() {
        return beans;
    }

    /**
     * 의존성의 의존성을 재귀적으로 체크하여 필요한 의존성의 객체를 모두 생성한다.
     *
     * @param con
     */
    private List<Object> resolveDependencies(String key, Constructor con) throws Exception {
        List<Object> dependencies = new ArrayList<>();
        if (beans.containsKey(key)) {
            dependencies.add(beans.get(key));
            return dependencies;
        }

        Class[] parameters = con.getParameterTypes();
        if (parameters.length == 0) {
            Object obj = con.newInstance();
            dependencies.add(obj);
        } else {
            for(Class clazz : parameters) {

                Object obj = beans.get(clazz.getName());
                if (obj == null) {
                    Constructor _con = clazz.getDeclaredConstructors()[0];

                    if (_con.getParameterTypes().length == 0) {
                        obj = _con.newInstance();
                    } else {
                        List<Object> deps = resolveDependencies(clazz.getName(), _con);
                        obj = _con.newInstance(deps.toArray());
                    }
                }
                dependencies.add(obj);
            }
        }
        return dependencies;
    }


    //얘는 뭐하는 메서드일까요? --> 내가쓰기 편하게 만든 경로를 클래스 객체로 반환후 classes 리스트에 저장.
    private List<Class> getClassNames(String rootPath, String packageName) {
        List<Class> classes = new ArrayList<>();
        List<File> files = getFiles(rootPath);
        // 매개변수 rootPath 기반으로 getFiles 메서드 실행한걸 files list에 반환.
        for (File file : files) {
            String path = file.getAbsolutePath();
            // 파일들의 절대경로를 반환한걸 path에 대입
            String className = packageName + "." + path.replace(rootPath + File.separator, "")
                    // replace는 target을 replacement로 바꿔준다는 의미인걸 기억하자.
                    //File.separator는 경로구분자를 의미 '/' 와 같은의미이다. // 앞의 루트패스와 /까지 지워준다
                    .replace(".class", "").replace(File.separator, ".");
            //뒤의 .class도 빈 문자열로 바꿔주고,  모든 /를 .으로 변환뒤 className에 대입
            // 그러니까 즉 /로 표시되어있던 주소들을 .로 바꿔주고 앞에  rootPath랑 뒤에 class 제거 형태로 만든다.
            try {
                Class cls = Class.forName(className); // className에 해당하는 클래스 객체를 반환함.
                classes.add(cls); // classes리스트에 반환값을 추가
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return classes;
    }


    // 이메서드의 역할은? --> rootPath기반으로 만든 파일객체가 가리키는 디렉토리와 하위 디렉토리들을 items 에 반환
    private List<File> getFiles(String rootPath) {
        List<File> items = new ArrayList<>();
        File[] files = new File(rootPath).listFiles();
        //rootPath기반으로 파일 객체를 만든후, 객체가 가리키는 디렉토리에 있는 파일과 하위 디렉토리들을 배열로 반환
        // 반환되는 배열은 해당 디렉토리 내의 파일들을 나타내고, 만약 디렉토리가 비어있으면 null을 반환합니다.
        if (files == null) return items;
        // 반환하는 파일이 null일경우에 items로 반환
        for (File file : files) {
            if (file.isDirectory()) { // 파일이 디렉토리인지 아닌지를 판단한다.
                List<File> _files = getFiles(file.getAbsolutePath());
                // 맞을경우 주어진 rootPath를 기반으 모든 파일 및 하위디렉토리들을 찾아서 리스트로 반환 후 해당 파일 객체의 절대 경로를 반환
                if (!_files.isEmpty()) items.addAll(_files);
                // 만약 비어있으면 items 리스트에 _files의 모든 요소추가
            } else {
                items.add(file); // 파일 디렉토리가 아닐경우 file만 items 리스트에 추가
            }
        }
        return items;
    }
}