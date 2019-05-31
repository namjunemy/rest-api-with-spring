package io.namjune.basicrestapi.index;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.namjune.basicrestapi.common.BaseControllerTest;
import org.junit.Test;

public class IndexControllerTest extends BaseControllerTest {

    @Test
    public void index() throws Exception {
        //다른 리소스에 대한 링크를 제공하기 위한 인덱스 생성 테스트
        this.mockMvc.perform(get("/api/"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("_links.events").exists());
    }
}
