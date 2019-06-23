package io.namjune.basicrestapi.index;

import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
            .andExpect(jsonPath("_links.events").exists()
            )
            .andDo(print())
            .andDo(document("index",
                links(
                    linkWithRel("events").description("서비스 진입점")
                )
            ))
        ;
    }
}
