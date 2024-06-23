package org.choongang.global.router;

import jakarta.servlet.http.HttpServletRequest;
import org.choongang.global.config.annotations.*;
import org.choongang.global.config.containers.BeanContainer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
//요청주소와 요청방식 정보를 가지고 컨트롤러 객체와 요청 메서드를 찾아주는 역할
public class HandlerMappingImpl implements HandlerMapping{

    private String controllerUrl;

    @Override
    public List<Object> search(HttpServletRequest request) {

        List<Object> items = getControllers();

        for (Object item : items) {
            /** Type 애노테이션에서 체크 S */
            // @RequestMapping, @GetMapping, @PostMapping, @PatchMapping, @PutMapping, @DeleteMapping
            if (isMatch(request,item.getClass().getDeclaredAnnotations(), false, null)) {
                // 메서드 체크
                for (Method m : item.getClass().getDeclaredMethods()) {
                    if (isMatch(request, m.getDeclaredAnnotations(), true, controllerUrl)) {
                        return List.of(item, m);
                    }
                }
            }
            /** Type 애노테이션에서 체크 E */

            /**
             * Method 애노테이션에서 체크 S
             *  - Type 애노테이션 주소 매핑이 되지 않은 경우, 메서드에서 패턴 체크
             */
            for (Method m : item.getClass().getDeclaredMethods()) {
                if (isMatch(request, m.getDeclaredAnnotations(), true, null)) {
                    return List.of(item, m);
                }
            }
            /* Method 애노테이션에서 체크 E */
        }

        return null;
    }

    /**
     *
     * @param request
     * @param annotations : 적용 애노테이션 목록
     * @param isMethod : 메서드의 에노테이션 체크인지
     * @param prefixUrl : 컨트롤러 체크인 경우 타입 애노테이션에서 적용된 경우
     * @return
     */

    private boolean isMatch(HttpServletRequest request, Annotation[] annotations, boolean isMethod, String prefixUrl) {

        String uri = request.getRequestURI();
        String method = request.getMethod().toUpperCase();
        String[] mappings = null;
        for (Annotation anno : annotations) {

            if (anno instanceof RequestMapping) { // 모든 요청 방식 매핑
                RequestMapping mapping = (RequestMapping) anno;
                mappings = mapping.value();
            } else if (anno instanceof GetMapping && method.equals("GET")) { // GET 방식 매핑
                GetMapping mapping = (GetMapping) anno;
                mappings = mapping.value();
            } else if (anno instanceof PostMapping && method.equals("POST")) {
                PostMapping mapping = (PostMapping) anno;
                mappings = mapping.value();
            } else if (anno instanceof PutMapping && method.equals("PUT")) {
                PutMapping mapping = (PutMapping) anno;
                mappings = mapping.value();
            } else if (anno instanceof PatchMapping && method.equals("PATCH")) {
                PatchMapping mapping = (PatchMapping) anno;
                mappings = mapping.value();
            } else if (anno instanceof DeleteMapping && method.equals("DELETE")) {
                DeleteMapping mapping = (DeleteMapping) anno;
                mappings = mapping.value();
            }

            if (mappings != null && mappings.length > 0) {

                String matchUrl = null;
                if (isMethod) {
                    String addUrl = prefixUrl == null ? "" : prefixUrl;
                    // 메서드인 경우 *와 {경로변수} 고려하여 처리
                    for(String mapping : mappings) {
                        String pattern = mapping.replace("/*", "/\\w*") // replace는 고정된 문자열 변경
                                .replaceAll("/\\{\\w+\\}", "/(\\\\w*)"); // 정규표현식에 해당하는 모든 문자열 변경

                        Pattern p = Pattern.compile("^" + request.getContextPath() + addUrl + pattern + "$");
                        Matcher matcher = p.matcher(uri);
                        return matcher.find();
                    }
                } else {
                    List<String> matches = Arrays.stream(mappings)
                            .filter(s -> uri.startsWith(request.getContextPath() + s)).toList();
                    if (!matches.isEmpty()) {
                        matchUrl = matches.get(0);
                        controllerUrl = matchUrl;
                    }
                }
                return matchUrl != null && !matchUrl.isBlank();
            }
        }

        return false;
    }

    /**
     * 모든 컨트롤러 조회
     *
     * @return
     */

        //BeanContainer에 등록된 모든 빈 객체들 중에서 @Controller 또는 @RestController 애노테이션이 선언된 객체들만을 필터링하여 리스트로 반환하는 기능
    private List<Object> getControllers() {
        return BeanContainer.getInstance().getBeans().entrySet() // BeanContainer의 instance를 가져온후에  맵형태의 beans가져와서 entrySet으로 변환
                .stream()
                .map(s -> s.getValue())// entrySet의 각 요소마다의 값을 반환한다.
                .filter(b -> Arrays.stream(b.getClass().getDeclaredAnnotations())
                            .anyMatch(a -> a instanceof Controller || a instanceof RestController))
                //애너테이션 배열을 스트림으로 반환한다.
                // 각요소의 클래스를 가져온뒤, 해당요소에서 직접적으로 선언된 애너테이션들을 가져오고,
                //Controller 또는 RestController 중 하나라도 있는지 확인한다. 있는것들만 걸러서
                .toList(); // List로 만든다.
    }
}
