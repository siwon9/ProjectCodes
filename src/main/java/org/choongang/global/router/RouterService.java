package org.choongang.global.router;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.choongang.global.config.annotations.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor // final 필드 또는 @NonNull 애너테이션이 붙은 필드를 파라미터로 받는 생성자를 자동 생성한다.
public class RouterService {

    private final HandlerMappingImpl handlerMapping;
    private final HandlerAdapterImpl handlerAdapter;

    // 여기에 생성장 생성안해도 위 두개의 매개변수를 가지는 생성자를 자동생성한다.
    /**
     * 컨트롤러 라우팅
     *
     */

    public void route(HttpServletRequest req, HttpServletResponse res) throws IOException {
        List<Object> data = handlerMapping.search(req);
        if (data == null) { // 처리 가능한 컨트롤러를 못찾은 경우 404 응답 코드
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // 찾은 컨트롤러 요청 메서드를 실행
        handlerAdapter.execute(req, res, data);
    }

}
